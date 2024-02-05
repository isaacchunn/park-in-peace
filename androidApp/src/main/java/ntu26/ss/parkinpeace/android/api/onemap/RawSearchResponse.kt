package ntu26.ss.parkinpeace.android.api.onemap

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class RawSearchResponse(
    val found: Int,
    val totalNumPages: Int,
    val pageNum: Int,
    val results: List<Inner>
) {
    @Serializable
    data class Inner(
        @JsonNames("SEARCHVAL") val searchVal: String,
        @JsonNames("BLK_NO") val blkNo: String = "",
        @JsonNames("ROAD_NAME") val roadName: String = "",
        @JsonNames("BUILDING") val building: String = "",
        @JsonNames("ADDRESS") val address: String = "",
        @JsonNames("POSTAL") val postal: String = "",
        @JsonNames("X") val x: String = "",
        @JsonNames("Y") val y: String = "",
        @JsonNames("LATITUDE") val latitude: String = "",
        @JsonNames("LONGITUDE") val longitude: String = ""
    )
}