package ntu26.ss.parkinpeace.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ParkingSystem {
    @SerialName("pip.parking_system/coupon") COUPON,
    @SerialName("pip.parking_system.electronic") ELECTRONIC
}