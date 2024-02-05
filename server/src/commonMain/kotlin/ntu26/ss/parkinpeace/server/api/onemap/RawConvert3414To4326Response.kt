package ntu26.ss.parkinpeace.server.api.onemap

import kotlinx.serialization.Serializable

@Serializable
data class RawConvert3414To4326Response(
    val latitude: Double,
    val longitude: Double
)