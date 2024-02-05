package ntu26.ss.parkinpeace.server.api.ura

import ntu26.ss.parkinpeace.models.ChargeType
import ntu26.ss.parkinpeace.models.Lot
import ntu26.ss.parkinpeace.models.ParkingSystem
import ntu26.ss.parkinpeace.models.VehicleType
import ntu26.ss.parkinpeace.server.api.ApiError
import ntu26.ss.parkinpeace.server.data.external.UraAvailability
import ntu26.ss.parkinpeace.server.data.external.UraCarpark
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException

suspend fun UraApi.getAllCarparks(): List<UraCarpark> =
    getNonSeasonCarparks().groupBy { it.ppCode }.mapValues { (k, v) ->
        val first = v.first()
        UraCarpark(
            uraId = k,
            name = first.ppName.trim(),
            epsg3414 = v.pickEpsg3414(),
            lots = v.flatMap { it.getLots() }.sortedBy(Lot::toString).distinct()
        )
    }.values.toList()

suspend fun UraApi.getAvailabilities(): List<UraAvailability> =
    getCarparkLots().groupBy { it.carparkNo }.mapValues { (k, v) ->
        UraAvailability(uraId = k,
            availabilities = v.associate { it.lotType.asVehicleTypeOrThrow() to it.lotsAvailable.toInt() })
    }.values.toList()

private fun RawCarparkListAndRates.getLots(): List<Lot> = listOf(
    Lot(
        vehicleType = vehCat.asVehicleCatOrThrow(),
        chargeType = ChargeType.WEEKDAY,
        startTime = startTime.asLocalTimeOrThrow(),
        endTime = endTime.asLocalTimeOrThrow(),
        rate = weekdayRate.asRateInCentsOrThrow(),
        minDuration = weekdayMin.asDurationInMinutesOrThrow(),
        capacity = parkCapacity,
        system = parkingSystem.asParkingSystemOrThrow()
    ),
    Lot(
        vehicleType = vehCat.asVehicleCatOrThrow(),
        chargeType = ChargeType.SATURDAY,
        startTime = startTime.asLocalTimeOrThrow(),
        endTime = endTime.asLocalTimeOrThrow(),
        rate = satdayRate.asRateInCentsOrThrow(),
        minDuration = satdayMin.asDurationInMinutesOrThrow(),
        capacity = parkCapacity,
        system = parkingSystem.asParkingSystemOrThrow()
    ),
    Lot(
        vehicleType = vehCat.asVehicleCatOrThrow(),
        chargeType = ChargeType.SUNDAY_PH,
        startTime = startTime.asLocalTimeOrThrow(),
        endTime = endTime.asLocalTimeOrThrow(),
        rate = sunPHRate.asRateInCentsOrThrow(),
        minDuration = sunPHMin.asDurationInMinutesOrThrow(),
        capacity = parkCapacity,
        system = parkingSystem.asParkingSystemOrThrow()
    ),
)

/**
 * Tries to get a coordinate representing a car lot if available
 * falls back to motorcycle and then heavy vehicle if no choices are available
 *
 * Note that this is not stable.
 */
private fun List<RawCarparkListAndRates>.pickEpsg3414(): String? =
    sortedBy { it.vehCat.asVehicleCatOrThrow() }.flatMap { it.geometries }
        .firstOrNull()?.coordinates

private fun String?.asVehicleTypeOrThrow() =
    UraI18n.parseVehicleTypeOrNull(this) ?: throw ApiError("Unable to parse $this as VehicleType")

private fun String?.asVehicleCatOrThrow() =
    UraI18n.parseVehicleCatOrNull(this) ?: throw ApiError("Unable to parse $this as VehicleType")

private fun String?.asLocalTimeOrThrow() =
    UraI18n.parseTimeOrNull(this) ?: throw ApiError("Unable to parse $this as LocalTime")

private fun String?.asRateInCentsOrThrow() =
    UraI18n.parseRateInCentsOrNull(this) ?: throw ApiError("Unable to parse $this as rate")

private fun String?.asDurationInMinutesOrThrow() =
    UraI18n.parseDurationInMinsOrNull(this) ?: throw ApiError("Unable to parse $this as duration")

private fun String?.asParkingSystemOrThrow() = UraI18n.parseParkingSystemOrNull(this)
    ?: throw ApiError("Unable to parse $this as ParkingSystem")

/**
 * Class with knowledge of URA-specific formats
 */
private object UraI18n {
    private val uraTimeFormatter: DateTimeFormatter =
        DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("hh.mm a").toFormatter()

    private val uraMinutesSpec: Regex = """(\d+) mins""".toRegex()

    fun parseVehicleCatOrNull(string: String?): VehicleType? = when (string) {
        "Car" -> VehicleType.CAR
        "Motorcycle" -> VehicleType.MOTORCYCLE
        "Heavy Vehicle" -> VehicleType.HEAVY_VEHICLE
        else -> null
    }

    fun parseVehicleTypeOrNull(string: String?): VehicleType? = when (string) {
        "C" -> VehicleType.CAR
        "M" -> VehicleType.MOTORCYCLE
        "H" -> VehicleType.HEAVY_VEHICLE
        else -> null
    }

    fun parseParkingSystemOrNull(string: String?): ParkingSystem? = when (string) {
        "C" -> ParkingSystem.COUPON
        "B" -> ParkingSystem.ELECTRONIC
        else -> null
    }

    /**
     * Parse URA specific time format to [java.time.LocalTime] object
     */
    fun parseTimeOrNull(string: String?): LocalTime? {
        if (string == null) return null
        return try {
            LocalTime.parse(string, uraTimeFormatter)
        } catch (e: DateTimeParseException) {
            null
        }
    }

    /**
     * Parse URA $ format to cents
     */
    fun parseRateInCentsOrNull(string: String?): Int? = string?.let {
        val rate = string.drop(1).toDouble() * 100
        rate.toInt()
    }

    /**
     * Parse URA minTime format to Int in minutes, where 30 is the default minimum.
     */
    fun parseDurationInMinsOrNull(string: String?): Int? = string?.let {
        uraMinutesSpec.matchEntire(it)?.destructured?.component1()?.toInt()
    }
}