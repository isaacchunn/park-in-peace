package ntu26.ss.parkinpeace.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class VehicleType {
    @SerialName("pip.vehicle_type/car") CAR,
    @SerialName("pip.vehicle_type/motorcycle") MOTORCYCLE,
    @SerialName("pip.vehicle_type/heavy_vehicle") HEAVY_VEHICLE
}