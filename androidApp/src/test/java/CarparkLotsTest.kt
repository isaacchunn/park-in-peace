import ntu26.ss.parkinpeace.android.models.Carpark
import ntu26.ss.parkinpeace.android.models.getApplicableLots
import ntu26.ss.parkinpeace.android.utils.isWithinDayParking
import ntu26.ss.parkinpeace.android.utils.isWithinNightParking
import ntu26.ss.parkinpeace.models.*
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class CarparkLotsTest {

    val carparkHarness: Carpark = Carpark(
        id = "test",
        ref = "test",
        name = "test",
        address = "test",
        epsg4326 = Coordinate.of(1.234, 103.145),
        lots = listOf(),
        features = listOf(),
        hash = "test"
    )

    val friday8AM = LocalDateTime.of(2023, Month.NOVEMBER, 10, 8, 0)
    val saturday8AM = LocalDateTime.of(2023, Month.NOVEMBER, 11, 8, 0)
    val sunday8AM = LocalDateTime.of(2023, Month.NOVEMBER, 12, 8, 0)
    val monday8AM = LocalDateTime.of(2023, Month.NOVEMBER, 13, 8, 0)

    @Test
    fun testApplicableLotsVehicles() {
        val lotHarness = Lot(
            vehicleType = VehicleType.CAR,
            chargeType = ChargeType.SATURDAY,
            startTime = LocalTime.of(7, 0),
            endTime = LocalTime.of(22, 30),
            rate = 60,
            minDuration = 30,
            capacity = 112,
            system = ParkingSystem.ELECTRONIC
        )

        val lots = listOf(
            lotHarness.copy(vehicleType = VehicleType.CAR),
            lotHarness.copy(vehicleType = VehicleType.MOTORCYCLE),
            lotHarness.copy(vehicleType = VehicleType.HEAVY_VEHICLE),

            )
        val test = carparkHarness.copy(lots = lots)
        for (lot in test.lots) {
            assertEquals(
                lot,
                test.getApplicableLots(
                    vehicleType = lot.vehicleType,
                    asof = saturday8AM.toInstant(ZoneOffset.UTC),
                    isHoliday = false
                ).singleOrNull(),
                "Vehicle type mismatch."
            )
        }
    }

    @Test
    fun testApplicableLotsChargeType() {
        val lotHarness = Lot(
            vehicleType = VehicleType.CAR,
            chargeType = ChargeType.WEEKDAY,
            startTime = LocalTime.of(7, 0),
            endTime = LocalTime.of(22, 30),
            rate = 60,
            minDuration = 30,
            capacity = 112,
            system = ParkingSystem.ELECTRONIC
        )
        val lots = listOf(
            lotHarness.copy(chargeType = ChargeType.WEEKDAY),
            lotHarness.copy(chargeType = ChargeType.SATURDAY),
            lotHarness.copy(chargeType = ChargeType.SUNDAY_PH)
        )
        val test = carparkHarness.copy(lots = lots)

        assertEquals(
            lots[0],
            test.getApplicableLots(
                vehicleType = VehicleType.CAR,
                asof = monday8AM.toInstant(ZoneOffset.UTC),
                isHoliday = false
            ).singleOrNull(),
            "ChargeType mismatch. Expected ${lots[0].chargeType} for MONDAY, 8AM"
        )

        assertEquals(
            lots[0],
            test.getApplicableLots(
                vehicleType = VehicleType.CAR,
                asof = friday8AM.toInstant(ZoneOffset.UTC),
                isHoliday = false
            ).singleOrNull(),
            "ChargeType mismatch. Expected ${lots[0].chargeType} for FRIDAY, 8AM"
        )

        assertEquals(
            lots[1],
            test.getApplicableLots(
                vehicleType = VehicleType.CAR,
                asof = saturday8AM.toInstant(ZoneOffset.UTC),
                isHoliday = false
            ).singleOrNull(),
            "ChargeType mismatch. Expected ${lots[1].chargeType} for SATURDAY, 8AM"
        )

        assertEquals(
            lots[2],
            test.getApplicableLots(
                vehicleType = VehicleType.CAR,
                asof = sunday8AM.toInstant(ZoneOffset.UTC),
                isHoliday = false
            ).singleOrNull(),
            "ChargeType mismatch. Expected ${lots[2].chargeType} for SUNDAY, 8AM"
        )

        assertEquals(
            lots[2],
            test.getApplicableLots(
                vehicleType = VehicleType.CAR,
                asof = sunday8AM.toInstant(ZoneOffset.UTC),
                isHoliday = true
            ).singleOrNull(),
            "ChargeType mismatch. Expected ${lots[2].chargeType} for SUNDAY, 8AM, isHoliday = true"
        )

        assertEquals(
            lots[2],
            test.getApplicableLots(
                vehicleType = VehicleType.CAR,
                asof = friday8AM.toInstant(ZoneOffset.UTC),
                isHoliday = true
            ).singleOrNull(),
            "ChargeType mismatch. Expected ${lots[2].chargeType} for FRIDAY, 8AM, isHoliday = true"
        )
    }

    @Test
    fun testLocalTimeDayParking() {
        val startTime = LocalTime.of(7, 0)
        val endTime = LocalTime.of(22, 30)
        val tests = listOf(
            LocalTime.of(6, 59, 59) to false,
            LocalTime.of(7, 0) to true,
            LocalTime.of(22, 30, 1) to false
        )

        for ((currentTime, expected) in tests) {
            assertEquals(expected, currentTime.isWithinDayParking(startTime, endTime))
        }

        for ((currentTime, _) in tests) {
            assertFalse(currentTime.isWithinDayParking(endTime, startTime))
        }
    }

    @Test
    fun testLocalTimeNightParking() {
        val startTime = LocalTime.of(22, 30)
        val endTime = LocalTime.of(7, 0)
        val tests = listOf(
            LocalTime.of(22, 29, 59) to false,
            LocalTime.of(6, 59, 59) to true,
            LocalTime.of(7, 0, 1) to false
        )

        for ((currentTime, expected) in tests) {
            assertEquals(expected, currentTime.isWithinNightParking(startTime, endTime))
        }

        for ((currentTime, _) in tests) {
            assertFalse(currentTime.isWithinNightParking(endTime, startTime))
        }
    }

    @Test
    fun testApplicableLotsMultivalid() {
        val lotHarness = Lot(
            vehicleType = VehicleType.CAR,
            chargeType = ChargeType.WEEKDAY,
            startTime = LocalTime.of(7, 0),
            endTime = LocalTime.of(22, 30),
            rate = 60,
            minDuration = 30,
            capacity = 112,
            system = ParkingSystem.ELECTRONIC
        )
        val lots = listOf(lotHarness, lotHarness, lotHarness)
        val test = carparkHarness.copy(lots = lots)

        assertEquals(
            3, test.getApplicableLots(
                vehicleType = lotHarness.vehicleType,
                asof = monday8AM.toInstant(ZoneOffset.UTC),
                isHoliday = false
            ).size
        )
    }
}
