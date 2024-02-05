package ntu26.ss.parkinpeace.server.api.onemap

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class RawGetTokenResponse(
    @JsonNames("access_token") val accessToken: String,
    @JsonNames("expiry_timestamp") val expiryTimeStamp: Long
)