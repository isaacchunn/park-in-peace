package ntu26.ss.parkinpeace.android.api.pip

import kotlinx.serialization.Serializable
import ntu26.ss.parkinpeace.android.models.CarparkAvailability

@Serializable
data class QueryNearbyResponse(
    val page: Int,
    val numPages: Int,
    val found: Int,
    val results: List<CarparkAvailability>
)