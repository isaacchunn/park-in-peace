package ntu26.ss.parkinpeace.server.api.lta

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RawLtaApiResponse<T>(
    @SerialName("odata.metadata") val metadata: String,
    val value: T
)
