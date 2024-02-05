package ntu26.ss.parkinpeace.server.models

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ntu26.ss.parkinpeace.models.Coordinate
import ntu26.ss.parkinpeace.models.VehicleType

@Serializable
data class CarparkAvailability(
    val id: String,
    val info: Carpark? = null,
    val origin: Coordinate? = null,
    val distance: Int? = null,
    val travelTime: Int? = null,
    val lots: Map<VehicleType, Inner>,
    val asof: Instant
) {

    @Serializable
    data class Inner(@SerialName("c") val current: Int, @SerialName("p") val predicted: Int)
}