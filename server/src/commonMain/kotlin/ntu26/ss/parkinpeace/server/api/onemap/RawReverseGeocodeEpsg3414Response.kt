package ntu26.ss.parkinpeace.server.api.onemap

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class RawReverseGeocodeEpsg3414Response(
    @JsonNames("GeocodeInfo") val geoCodeInfo: List<Inner>
) {
    @Serializable
    data class Inner(
        @JsonNames("BUILDINGNAME") val buildingName: String = "",
        @JsonNames("BLOCK") val block: String = "",
        @JsonNames("ROAD") val road: String,
        @JsonNames("POSTALCODE") val postalCode: String = "",
        @JsonNames("XCOORD") val x: String,
        @JsonNames("YCOORD") val y: String,
        @JsonNames("LATITUDE") val latitude: String,
        @JsonNames("LONGITUDE") val longitude: String
    )
}
