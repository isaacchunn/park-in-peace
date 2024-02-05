package ntu26.ss.parkinpeace.server.data.external

import ntu26.ss.parkinpeace.models.VehicleType

data class UraAvailability(
    val uraId: String,
    val availabilities: Map<VehicleType, Int>
)

fun UraAvailability.asExternalAvailabilitySource(): ExternalAvailabilitySource =
    ExternalAvailabilitySource.Ura(this)
