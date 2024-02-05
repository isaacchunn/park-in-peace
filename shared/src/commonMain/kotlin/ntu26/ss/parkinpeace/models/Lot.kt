package ntu26.ss.parkinpeace.models

import kotlinx.serialization.Serializable
import ntu26.ss.parkinpeace.serializers.LocalTimeSerializer
import java.time.LocalTime

@Serializable
data class Lot(
    val vehicleType: VehicleType,
    val chargeType: ChargeType,
    @Serializable(with = LocalTimeSerializer::class) val startTime: LocalTime,
    @Serializable(with = LocalTimeSerializer::class) val endTime: LocalTime,
    val rate: Int,
    val minDuration: Int,
    val capacity: Int,
    val system: ParkingSystem
)