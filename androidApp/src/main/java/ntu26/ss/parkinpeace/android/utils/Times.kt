package ntu26.ss.parkinpeace.android.utils

import java.time.LocalTime

/**
 * Checks if [this] falls within [start] and [end] for DAY PARKING
 * @see isWithinNightParking
 * @see isWithinParking
 */
fun LocalTime.isWithinDayParking(start: LocalTime, end: LocalTime): Boolean {
    val isStartBeforeEnd = start < end
    val isAfterStart = start <= this
    val isBeforeEnd = this <= end
    return isStartBeforeEnd && isAfterStart && isBeforeEnd
}

/**
 * Checks if [this] falls after [end] and before [start] for NIGHT PARKING
 *
 * According to [HDB](https://www.hdb.gov.sg/car-parks/shortterm-parking/coupon-parking),
 * Night Parking generally starts after 1030 PM
 * @see isWithinDayParking
 * @see isWithinParking
 */
fun LocalTime.isWithinNightParking(start: LocalTime, end: LocalTime): Boolean {
    val isStartAfterEnd = start > end
    if (isStartAfterEnd) {
        require(start >= LocalTime.of(22, 30)) { "Not night parking" }
    }
    val isAfterMidnight = this >= LocalTime.MIDNIGHT
    return isStartAfterEnd && when (isAfterMidnight) {
        true -> start <= LocalTime.MIDNIGHT.minusNanos(1) && this <= end
        false -> start <= this && LocalTime.MIDNIGHT <= end
    }
}

/**
 * Checks if [this] is within day parking or night parking within the time-spans specified.
 * @see isWithinDayParking
 * @see isWithinNightParking
 */
fun LocalTime.isWithinParking(start: LocalTime, end: LocalTime): Boolean {
    return isWithinDayParking(start, end) || isWithinNightParking(start, end)
}