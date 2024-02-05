package ntu26.ss.parkinpeace.server.api.ura

import kotlinx.serialization.Serializable

typealias RawCarparkLotsResponse = RawUraApiResponse<List<RawCarparkLotsInner>>

@Serializable
data class RawCarparkLotsInner(
    val carparkNo: String,
    val geometries: List<RawGeometry>,
    val lotType: String,
    val lotsAvailable: String
)
