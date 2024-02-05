package ntu26.ss.parkinpeace.server.data.external

import ntu26.ss.parkinpeace.models.Coordinate
import ntu26.ss.parkinpeace.models.VehicleType
import ntu26.ss.parkinpeace.server.data.Ref

data class LtaCarparkAvailability(
    val ref: Ref,
    val name: String,
    val epsg4326: Coordinate,
    val availability: Int,
    val lotType: VehicleType,
)

fun LtaCarparkAvailability.asExternalCarparkSource(): ExternalCarparkSource =
    ExternalCarparkSource.Lta(this)

fun LtaCarparkAvailability.asExternalAvailabilitySource(): ExternalAvailabilitySource =
    ExternalAvailabilitySource.Lta(this)
