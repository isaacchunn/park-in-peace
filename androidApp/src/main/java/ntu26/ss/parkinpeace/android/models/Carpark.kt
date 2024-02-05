package ntu26.ss.parkinpeace.android.models

import kotlinx.serialization.Serializable
import ntu26.ss.parkinpeace.Hashable
import ntu26.ss.parkinpeace.android.utils.isWithinParking
import ntu26.ss.parkinpeace.models.*
import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId

@Serializable
data class Carpark(
    val id: String,
    val ref: String,
    override val name: String,
    override val address: String,
    override val epsg4326: Coordinate,
    val lots: List<Lot>,
    val features: List<Feature>,
    override val hash: String
) : Location, Hashable

/**
 * Get applicable [Carpark.lots] based on time of day.
 *
 * Checks all [Carpark.lots] to see if they are applicable to [vehicleType] and [asof].
 * @return list of applicable [Lot]s with pricing information, if any, or empty list if no lots were applicable.
 */
fun Carpark.getApplicableLots(
    vehicleType: VehicleType,
    asof: Instant = Clock.systemUTC().instant(),
    isHoliday: Boolean = false
): List<Lot> {
    val currentDateTime = asof.atZone(ZoneId.systemDefault())
    val currentLocalTime = currentDateTime.toLocalTime()
    val chargeType = when (isHoliday) {
        true -> ChargeType.SUNDAY_PH
        false -> currentDateTime.dayOfWeek.asChargeType()
    }
    return lots.filter { it.chargeType == chargeType && it.vehicleType == vehicleType }
        .filter { currentLocalTime.isWithinParking(it.startTime, it.endTime) }
        .sortedBy { it.rate }
}

/**
 * Gets the lot with the lowest rate among [this] or null if the list was empty.
 *
 * Use this in conjunction with [getApplicableLots] to get an applicable lot with the lowest price.
 *
 * @see getApplicableLots
 */
fun List<Lot>.minPriceOrNull(): Lot? = minByOrNull { it.rate }

private fun DayOfWeek.asChargeType(): ChargeType = when (this) {
    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY -> ChargeType.WEEKDAY
    DayOfWeek.SATURDAY -> ChargeType.SATURDAY
    DayOfWeek.SUNDAY -> ChargeType.SUNDAY_PH
}
