package ntu26.ss.parkinpeace.server.data

sealed class Ref(value: String)
data class UraRef(val value: String) : Ref(value)
data class LtaRef(val value: String) : Ref(value)
data class HdbRef(val value: String) : Ref(value)

val Ref.normalized: String
    get() = when (this) {
        is UraRef -> "ura/$value"
        is LtaRef -> "lta/$value"
        is HdbRef -> "hdb/$value"
    }