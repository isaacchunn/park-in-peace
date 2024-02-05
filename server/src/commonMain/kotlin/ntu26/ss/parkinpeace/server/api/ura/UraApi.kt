package ntu26.ss.parkinpeace.server.api.ura

interface UraApi {
    suspend fun getCarparkLots(): List<RawCarparkLotsInner>
    suspend fun getNonSeasonCarparks(): List<RawCarparkListAndRates>
}