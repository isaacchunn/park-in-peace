package ntu26.ss.parkinpeace.server.models

import kotlinx.serialization.Serializable

@Serializable
data class QueryNearbyResponse(
    val page: Int,
    val numPages: Int,
    val found: Int,
    val results: List<CarparkAvailability>
)