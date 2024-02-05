package ntu26.ss.parkinpeace.server.models

import kotlinx.serialization.Serializable
import ntu26.ss.parkinpeace.Hashable
import ntu26.ss.parkinpeace.models.Coordinate
import ntu26.ss.parkinpeace.models.Feature
import ntu26.ss.parkinpeace.models.Location
import ntu26.ss.parkinpeace.models.Lot
import ntu26.ss.parkinpeace.server.serializers.ULIDSerializer
import ulid.ULID

@Serializable
data class Carpark(
    @Serializable(with = ULIDSerializer::class) val id: ULID,
    val ref: String,
    override val name: String,
    override val address: String,
    override val epsg4326: Coordinate,
    val lots: List<Lot>,
    val features: List<Feature>,
    override val hash: String
) : Location, Hashable