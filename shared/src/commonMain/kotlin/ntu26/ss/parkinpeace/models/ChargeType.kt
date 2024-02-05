package ntu26.ss.parkinpeace.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ChargeType {
    @SerialName("pip.charge_type/weekday") WEEKDAY,
    @SerialName("pip.charge_type/saturday") SATURDAY,
    @SerialName("pip.charge_type/sunday_ph") SUNDAY_PH
}