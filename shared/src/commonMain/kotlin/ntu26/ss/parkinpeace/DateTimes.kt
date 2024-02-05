package ntu26.ss.parkinpeace

import java.time.LocalTime

fun LocalTime.toMinutesOfDay(): Int = hour * 60 + minute
fun fromMinutesOfDay(minutes: Int): LocalTime = LocalTime.of(minutes / 60, minutes % 60)