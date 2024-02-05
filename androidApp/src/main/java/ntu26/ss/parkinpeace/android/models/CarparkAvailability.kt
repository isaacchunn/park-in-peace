package ntu26.ss.parkinpeace.android.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import ntu26.ss.parkinpeace.models.Coordinate
import ntu26.ss.parkinpeace.models.VehicleType
import ntu26.ss.parkinpeace.serializers.InstantSerializer
import java.time.Instant

@Serializable
data class CarparkAvailability(
    val id: String,
    val info: Carpark? = null,
    val origin: Coordinate? = null,
    val distance: Int? = null,
    val travelTime: Int? = null,
    val lots: Map<VehicleType, Inner>,
    @Serializable(with = InstantSerializer::class) val asof: Instant
) {
    @Serializable
    data class Inner(@JsonNames("c") val current: Int, @JsonNames("p") val predicted: Int)
}