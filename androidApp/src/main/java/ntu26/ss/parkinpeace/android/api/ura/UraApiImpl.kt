package ntu26.ss.parkinpeace.android.api.ura

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import ntu26.ss.parkinpeace.android.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header

@Deprecated("This API will be removed from the client and migrated to the server in the near future.")
class UraApiImpl : UraApi {
    private val ACCESS_KEY = BuildConfig.ANDROID_URA_ACCESS_TOKEN
    private val BASE_URL = "https://www.ura.gov.sg/uraDataService/"

    /**
     *
     */
    private val service: Service by lazy {
        val retroFit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
        retroFit.create(Service::class.java)
    }

    private val token: String? by lazy {
        val t = runBlocking(Dispatchers.IO) { service.getToken(ACCESS_KEY) }

        when (t.status) {
            "Success" -> t.token!!
            else -> null
        }
    }

    /**
     * Getter function to check if our token isnt null
     */
    override val authenticated: Boolean
        get() = token != null

    /**
     * Function that interacts with the service to return the number of available lots for
     * URA carparks
     */
    override suspend fun getCarparkLots(): AvailableLotsResponse {
        return service.getCarparkLots(ACCESS_KEY, token!!)
    }

    /**
     * Function that interacts with the service to return the list of carparks and their rates
     */
    override suspend fun getCarparkListAndRates(): CarparkListResponse {
        return service.getCarparkListAndRates(ACCESS_KEY, token!!)
    }

    /**
     * Function that interacts with the service to return the list of seasonal carparks
     */
    override suspend fun getSeasonalCarparkList(): SeasonalCarparkListResponse {
        return service.getSeasonalCarparkListAndRates(ACCESS_KEY, token!!)
    }

    /**
     * We make an inner class to access instance variables from the outer scope
     * This works because inner classes are not static, they are instances
     */
    private interface Service {
        @GET("insertNewToken.action")
        suspend fun getToken(@Header("AccessKey") accessKey: String): Token

        @GET("invokeUraDS?service=Car_Park_Availability")
        suspend fun getCarparkLots(
            @Header("AccessKey") accessKey: String,
            @Header("Token") tokenAddress: String
        ): AvailableLotsResponse

        @GET("invokeUraDS?service=Car_Park_Details")
        suspend fun getCarparkListAndRates(
            @Header("AccessKey") accessKey: String,
            @Header("Token") tokenAddress: String
        ): CarparkListResponse

        @GET("invokeUraDS?service=Season_Car_Park_Details")
        suspend fun getSeasonalCarparkListAndRates(
            @Header("AccessKey") accessKey: String,
            @Header("Token") tokenAddress: String
        ): SeasonalCarparkListResponse

    }
}