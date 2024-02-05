package ntu26.ss.parkinpeace.server.api.lta

import kotlinx.datetime.Clock
import ntu26.ss.parkinpeace.server.api.getJsonConverterFactory
import ntu26.ss.parkinpeace.server.api.guarded
import ntu26.ss.parkinpeace.server.services.Secrets
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Header

class LtaApiImpl(private val clock: Clock = Clock.System) : LtaApi {
    private val ACCESS_KEY = Secrets.LTA_KEY
    private val BASE_URL = "http://datamall2.mytransport.sg/ltaodataservice/"
    private val service: Service by lazy {
        val retroFit =
            Retrofit.Builder().addConverterFactory(getJsonConverterFactory()).baseUrl(BASE_URL)
                .build()
        retroFit.create(Service::class.java)
    }

    override suspend fun getCarparkAvailability(): List<RawCarparkAvailability> =
        this@LtaApiImpl.guarded { service.getCarparkAvailability(ACCESS_KEY).value }

    private interface Service {
        @GET("CarParkAvailabilityv2")
        suspend fun getCarparkAvailability(@Header("AccountKey") accessKey: String): RawCarparkAvailabilityResponse
    }
}