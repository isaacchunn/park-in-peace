package ntu26.ss.parkinpeace.server.api.lta

interface LtaApi {
    suspend fun getCarparkAvailability(): List<RawCarparkAvailability>
}