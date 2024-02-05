package ntu26.ss.parkinpeace.android.api.ura

interface UraApi {
    val authenticated: Boolean
    suspend fun getCarparkLots(): AvailableLotsResponse
    suspend fun getCarparkListAndRates(): CarparkListResponse
    suspend fun getSeasonalCarparkList(): SeasonalCarparkListResponse
}