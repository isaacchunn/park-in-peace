package ntu26.ss.parkinpeace.server.api.lta

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

typealias RawCarparkAvailabilityResponse = RawLtaApiResponse<List<RawCarparkAvailability>>

@Serializable
data class RawCarparkAvailability(
    @JsonNames("CarParkID") val carparkId: String,
    @JsonNames("Area") val area: String,
    @JsonNames("Development") val development: String,
    @JsonNames("Location") val location: String,
    @JsonNames("AvailableLots") val availableLots: Int,
    @JsonNames("LotType") val lotType: String,
    @JsonNames("Agency") val agency: String
)
