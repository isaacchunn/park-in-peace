package ntu26.ss.parkinpeace.server.api.datagovsg

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class RawCarparkAvailabilityResponse(
    val items: List<Inner1>
) {
    @Serializable
    data class Inner1(
        val timestamp: Instant,
        @JsonNames("carpark_data") val carparkData: List<Inner2>
    )

    @Serializable
    data class Inner2(
        @JsonNames("carpark_number") val carparkNumber: String,
        @JsonNames("update_datetime") val update_datetime: LocalDateTime,
        @JsonNames("carpark_info") val carparkInfo: List<Inner3>
    )

    @Serializable
    data class Inner3(
        @JsonNames("total_lots") val totalLots: Int,
        @JsonNames("lot_type") val lotType: String,
        @JsonNames("lots_available") val lotsAvailable: Int
    )
}