package ntu26.ss.parkinpeace.server.data.external

import kotlinx.datetime.Instant
import ntu26.ss.parkinpeace.models.VehicleType
import ntu26.ss.parkinpeace.server.data.Ref

data class DatagovAvailability(
    val ref: Ref,
    val vehicleType: VehicleType,
    val availability: Int,
    val capacity: Int,
    val asof: Instant
)

fun DatagovAvailability.asExternalAvailabilitySource(): ExternalAvailabilitySource =
    ExternalAvailabilitySource.Datagov(this)
