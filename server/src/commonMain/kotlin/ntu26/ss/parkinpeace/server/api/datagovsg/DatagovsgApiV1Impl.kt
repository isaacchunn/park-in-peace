package ntu26.ss.parkinpeace.server.api.datagovsg

import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import ntu26.ss.parkinpeace.server.api.getJsonConverterFactory
import ntu26.ss.parkinpeace.server.api.guarded
import retrofit2.Retrofit
import retrofit2.http.GET
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.inputStream

class DatagovsgApiV1Impl(private val clock: Clock = Clock.System) : DatagovsgApi {
    private val BASE_URL = "https://api.data.gov.sg/"
    private val service: Service by lazy {
        val retroFit =
            Retrofit.Builder().addConverterFactory(getJsonConverterFactory()).baseUrl(BASE_URL)
                .build()
        retroFit.create(Service::class.java)
    }

    override suspend fun getCarparkAvailability(): RawCarparkAvailabilityResponse =
        this@DatagovsgApiV1Impl.guarded { service.getCarparkAvailability() }

    override suspend fun getCarparks(): RawCarparksResponse {
        return this@DatagovsgApiV1Impl.guarded {
            Path("src/commonMain/resources/static/hdb.json").absolute().inputStream().use {
                Json.decodeFromStream<RawCarparksResponse>(it)
            }
        }
    }

    private interface Service {
        @GET("v1/transport/carpark-availability")
        suspend fun getCarparkAvailability(): RawCarparkAvailabilityResponse
    }
}