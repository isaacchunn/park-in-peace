package ntu26.ss.parkinpeace.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Feature {
    @SerialName("pip.feature/vehicle_washing") VEHICLE_WASHING,
    @SerialName("pip.feature/electric_charging") ELECTRIC_CHARGING
}