package ntu26.ss.parkinpeace.server.api.datagovsg

interface DatagovsgApi {
    suspend fun getCarparkAvailability(): RawCarparkAvailabilityResponse
    suspend fun getCarparks(): RawCarparksResponse
}