package ntu26.ss.parkinpeace.server.api.ura

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RawUraApiResponse<T>(
    @SerialName("Status") val status: String,
    @SerialName("Message") val message: String,
    @SerialName("Result") val result: T
)
