package ntu26.ss.parkinpeace.server.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import ntu26.ss.parkinpeace.models.VehicleType
import ntu26.ss.parkinpeace.server.serializers.ULIDSerializer
import ulid.ULID

@Serializable
data class Availability(
    @Serializable(with = ULIDSerializer::class) val carparkId: ULID,
    val vehicleType: VehicleType,
    val asof: Instant,
    val availability: Int
)