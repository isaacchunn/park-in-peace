package ntu26.ss.parkinpeace.server.api.mapbox

import kotlinx.serialization.Serializable

@Serializable
data class RawDirectionsMatrixResponse(
    val code: String,
    val distances: List<List<Double>>,
    val durations: List<List<Double>>
)