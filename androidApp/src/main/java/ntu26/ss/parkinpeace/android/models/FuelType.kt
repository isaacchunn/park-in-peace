package ntu26.ss.parkinpeace.android.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class FuelType {
    @SerialName("pip.fuel_type/gasoline") GASOLINE,
    @SerialName("pip.fuel_type/electric") ELECTRIC,
    @SerialName("pip.fuel_type/hybrid") HYBRID
}