package ntu26.ss.parkinpeace.server.data.external

import ntu26.ss.parkinpeace.server.data.Ref
import ntu26.ss.parkinpeace.server.data.UraRef

sealed interface ExternalCarparkSource {
    data class Lta(val value: LtaCarparkAvailability) : ExternalCarparkSource
    data class Ura(val value: UraCarpark) : ExternalCarparkSource

    data class Datagov(val value: DatagovCarpark) : ExternalCarparkSource
}

sealed interface ExternalAvailabilitySource {
    data class Lta(val value: LtaCarparkAvailability) : ExternalAvailabilitySource
    data class Ura(val value: UraAvailability) : ExternalAvailabilitySource

    data class Datagov(val value: DatagovAvailability) : ExternalAvailabilitySource
}

fun ExternalCarparkSource.asRef(): Ref {
    return when (this) {
        is ExternalCarparkSource.Lta -> value.ref
        is ExternalCarparkSource.Ura -> UraRef(value.uraId)
        is ExternalCarparkSource.Datagov -> value.ref
    }
}

fun ExternalAvailabilitySource.asRef(): Ref {
    return when (this) {
        is ExternalAvailabilitySource.Lta -> value.ref
        is ExternalAvailabilitySource.Ura -> UraRef(value.uraId)
        is ExternalAvailabilitySource.Datagov -> value.ref
    }
}