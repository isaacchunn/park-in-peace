package ntu26.ss.parkinpeace.server.api.ura

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ntu26.ss.parkinpeace.server.api.ApiError
import ntu26.ss.parkinpeace.server.api.getJsonConverterFactory
import ntu26.ss.parkinpeace.server.api.guarded
import ntu26.ss.parkinpeace.server.services.Secrets
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Header
import kotlin.time.Duration.Companion.hours

class UraApiImpl(private val clock: Clock = Clock.System) : UraApi {
    private val ACCESS_KEY = Secrets.URA_TOKEN
    private val BASE_URL = "https://www.ura.gov.sg/uraDataService/"
    private val TOKEN_REFRESH_DURATION = 12.hours

    private val service: Service by lazy {
        val retroFit =
            Retrofit.Builder().addConverterFactory(getJsonConverterFactory()).baseUrl(BASE_URL)
                .build()
        retroFit.create(Service::class.java)
    }

    private var __token: String? = null
    private var __tokenAcquired: Instant = Instant.DISTANT_PAST
    private val __tokenLock = Mutex()

    private val hasTokenExpired: Boolean get() = clock.now() - __tokenAcquired >= TOKEN_REFRESH_DURATION

    /**
     * Rotates the token every [TOKEN_REFRESH_DURATION]
     */
    suspend fun getToken(): String {
        if (!hasTokenExpired) {
            val tok = __token
            if (tok != null) return tok
        }

        val token: String = __tokenLock.withLock {
            val tok = __token
            when (hasTokenExpired || tok == null) {
                false -> tok
                true -> coroutineScope {
                    val result = this@UraApiImpl.guarded { service.getToken(ACCESS_KEY) }.let {
                        when (it.status) {
                            "Success" -> it.result
                            else -> null
                        }
                    }
                    __token = result
                    __tokenAcquired = clock.now()
                    result
                }
            }
        } ?: throw ApiError("Unable to retrieve token from Ura Api")

        return token
    }


    /**
     * Function that interacts with the service to return the number of available lots for
     * URA carparks
     */
    override suspend fun getCarparkLots(): List<RawCarparkLotsInner> {
        return this@UraApiImpl.guarded { service.getCarparkLots(ACCESS_KEY, getToken()).result }
    }

    /**
     * Function that interacts with the service to return the list of carparks and their rates
     */
    override suspend fun getNonSeasonCarparks(): List<RawCarparkListAndRates> {
        return this@UraApiImpl.guarded {
            service.getCarparkListAndRates(
                ACCESS_KEY,
                getToken()
            ).result
        }
    }

    private interface Service {
        @GET("insertNewToken.action")
        suspend fun getToken(@Header("AccessKey") accessKey: String): RawGetTokenResponse

        @GET("invokeUraDS?service=Car_Park_Availability")
        suspend fun getCarparkLots(
            @Header("AccessKey") accessKey: String, @Header("Token") tokenAddress: String
        ): RawCarparkLotsResponse

        @GET("invokeUraDS?service=Car_Park_Details")
        suspend fun getCarparkListAndRates(
            @Header("AccessKey") accessKey: String, @Header("Token") tokenAddress: String
        ): RawCarparkListAndRatesResponse
    }
}