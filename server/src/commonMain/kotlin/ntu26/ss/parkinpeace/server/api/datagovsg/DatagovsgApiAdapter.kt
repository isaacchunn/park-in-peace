package ntu26.ss.parkinpeace.server.api.datagovsg

import ntu26.ss.parkinpeace.models.ChargeType
import ntu26.ss.parkinpeace.models.Lot
import ntu26.ss.parkinpeace.models.ParkingSystem
import ntu26.ss.parkinpeace.models.VehicleType
import ntu26.ss.parkinpeace.server.api.ApiError
import ntu26.ss.parkinpeace.server.data.HdbRef
import ntu26.ss.parkinpeace.server.data.external.DatagovAvailability
import ntu26.ss.parkinpeace.server.data.external.DatagovCarpark
import java.time.LocalTime

suspend fun DatagovsgApi.getAllCarparks(): List<DatagovCarpark> =
    getCarparks().map { cp ->
        DatagovCarpark(
            ref = HdbRef(cp.carparkNo),
            name = cp.address,
            epsg3414 = "${cp.xCoord},${cp.yCoord}",
            lots = hdbLotConfig {
                freeParking = cp.freeParking.hasFreeParkingOrThrow()
                nightParking = cp.nightParking.hasNightParkingOrThrow()
                parkingSystem = cp.typeOfParkingSystem.asParkingSystemOrThrow()
            }
        )
    }

suspend fun DatagovsgApi.getAvailabilities(): List<DatagovAvailability> =
    getCarparkAvailability().items.flatMap { r1 ->
        val asof = r1.timestamp
        r1.carparkData.flatMap { r2 ->
            r2.carparkInfo
                .filter {
                    it.lotType !in setOf(
                        "S",
                        "L",
                        "Y"
                    )
                } // S is likely season parking lot, L is likely loading bay
                .map {
                    DatagovAvailability(
                        ref = HdbRef(r2.carparkNumber),
                        vehicleType = it.lotType.asVehicleTypeOrThrow(),
                        availability = it.lotsAvailable,
                        capacity = it.totalLots,
                        asof = asof
                    )
                }
        }
    }

private fun String?.asVehicleTypeOrThrow() =
    DatagovsgI18n.parseVehicleTypeOrNull(this) ?: throw ApiError("Unable to parse $this as VehicleType")

private fun String?.asParkingSystemOrThrow() = DatagovsgI18n.parseParkingSystemOrNull(this)
    ?: throw ApiError("Unable to parse $this as ParkingSystem")

private fun String?.hasFreeParkingOrThrow() =
    DatagovsgI18n.parseFreeParkingOrNull(this) ?: throw ApiError("Unable to parse $this as Free Parking")

private fun String?.hasNightParkingOrThrow() =
    DatagovsgI18n.parseNightParkingOrNull(this) ?: throw ApiError("Unable to parse $this as Night Parking")

/**
 * Class with knowledge of data.gov.sg-specific formats
 */
private object DatagovsgI18n {

    fun parseVehicleTypeOrNull(string: String?): VehicleType? = when (string) {
        "C" -> VehicleType.CAR
        "M" -> VehicleType.MOTORCYCLE
        "H" -> VehicleType.HEAVY_VEHICLE
        else -> null
    }

    fun parseParkingSystemOrNull(string: String?): ParkingSystem? = when (string) {
        "COUPON PARKING" -> ParkingSystem.COUPON
        "ELECTRONIC PARKING" -> ParkingSystem.ELECTRONIC
        else -> null
    }

    fun parseFreeParkingOrNull(string: String?): Boolean? = when (string) {
        "SUN & PH FR 7AM-10.30PM" -> true
        "SUN & PH FR 1PM-10.30PM" -> true
        "NO" -> false
        else -> null
    }

    fun parseNightParkingOrNull(string: String?): Boolean? = when (string) {
        "YES" -> true
        "NO" -> false
        else -> null
    }
}

private class HdbLotConfiguration(
    var nightParking: Boolean = false,
    var freeParking: Boolean = false,
    var parkingSystem: ParkingSystem = ParkingSystem.ELECTRONIC
) {
    fun build(): List<Lot> {
        val items = DEFAULT_LOTS.toMutableList()

        if (nightParking) {
            items.addAll(DEFAULT_NIGHT_LOTS)
        }
        when {
            freeParking && nightParking -> items.addAll(DEFAULT_SUNDAY_FREE_LOTS + DEFAULT_SUNDAY_NIGHT_FREE_LOTS)
            freeParking && !nightParking -> items.addAll(DEFAULT_SUNDAY_FREE_LOTS)
            !freeParking && nightParking -> items.addAll(DEFAULT_SUNDAY_LOTS + DEFAULT_SUNDAY_NIGHT_LOTS)
            !freeParking && !nightParking -> items.addAll(DEFAULT_SUNDAY_LOTS)
        }

        return items.map { it.copy(system = parkingSystem) }.sortedBy { it.toString() }
    }

    companion object {
        private val basic: Lot = Lot(
            vehicleType = VehicleType.CAR,
            chargeType = ChargeType.WEEKDAY,
            startTime = LocalTime.of(7, 0),
            endTime = LocalTime.of(22, 30),
            rate = 60,
            minDuration = 30,
            capacity = 0,
            system = ParkingSystem.ELECTRONIC
        )

        private val DEFAULT_LOTS = listOf(
            basic,
            basic.copy(chargeType = ChargeType.SATURDAY),
            basic.copy(vehicleType = VehicleType.MOTORCYCLE, rate = 65, minDuration = 930),
            basic.copy(
                vehicleType = VehicleType.MOTORCYCLE,
                chargeType = ChargeType.SATURDAY,
                rate = 65,
                minDuration = 930
            )
        )

        private val DEFAULT_NIGHT_LOTS = listOf(
            basic.copy(startTime = LocalTime.of(22, 30), endTime = LocalTime.of(7, 0)),
            basic.copy(
                chargeType = ChargeType.SATURDAY,
                startTime = LocalTime.of(22, 30),
                endTime = LocalTime.of(7, 0)
            ),
            basic.copy(
                vehicleType = VehicleType.MOTORCYCLE,
                rate = 65,
                minDuration = 570,
                startTime = LocalTime.of(22, 30),
                endTime = LocalTime.of(7, 0)
            ),
            basic.copy(
                vehicleType = VehicleType.MOTORCYCLE,
                chargeType = ChargeType.SATURDAY,
                rate = 65,
                minDuration = 570,
                startTime = LocalTime.of(22, 30),
                endTime = LocalTime.of(7, 0)
            )
        )

        private val DEFAULT_SUNDAY_LOTS = listOf(
            basic.copy(chargeType = ChargeType.SUNDAY_PH),
            basic.copy(
                vehicleType = VehicleType.MOTORCYCLE,
                chargeType = ChargeType.SUNDAY_PH,
                rate = 65,
                minDuration = 930
            )
        )

        private val DEFAULT_SUNDAY_NIGHT_LOTS = listOf(
            basic.copy(
                chargeType = ChargeType.SUNDAY_PH,
                startTime = LocalTime.of(22, 30),
                endTime = LocalTime.of(7, 0)
            ),
            basic.copy(
                vehicleType = VehicleType.MOTORCYCLE,
                chargeType = ChargeType.SUNDAY_PH,
                rate = 65,
                minDuration = 570,
                startTime = LocalTime.of(22, 30),
                endTime = LocalTime.of(7, 0)
            )
        )

        private val DEFAULT_SUNDAY_FREE_LOTS = DEFAULT_SUNDAY_LOTS.map { it.copy(rate = 0) }
        private val DEFAULT_SUNDAY_NIGHT_FREE_LOTS = DEFAULT_SUNDAY_NIGHT_LOTS.map { it.copy(rate = 0) }
    }
}

private fun hdbLotConfig(block: HdbLotConfiguration.() -> Unit): List<Lot> = HdbLotConfiguration().apply {
    block(this)
}.build()
