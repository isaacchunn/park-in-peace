package ntu26.ss.parkinpeace.android.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringArrayResource
import ntu26.ss.parkinpeace.android.R
import java.time.Instant
import java.time.ZoneId

@Composable
/**
 * Checks whether the given time is a holiday by offline lookup
 */
fun Instant.isHoliday(): Boolean {
    val currentDate = this.atZone(ZoneId.systemDefault()).toLocalDate()
    val holidays = when (currentDate.year) {
        2023 -> stringArrayResource(R.array.Y2023)
        2024 -> stringArrayResource(R.array.Y2024)
        else -> throw IllegalArgumentException("isHoliday has not been updated for year ${currentDate.year}")
    }
    val cdts = currentDate.toString()
    return holidays.any { cdts == it }
}