package ntu26.ss.parkinpeace.android.api.pip

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ntu26.ss.parkinpeace.IOFlow
import ntu26.ss.parkinpeace.android.BuildConfig
import ntu26.ss.parkinpeace.android.models.Carpark
import ntu26.ss.parkinpeace.android.models.CarparkAvailability
import ntu26.ss.parkinpeace.models.Coordinate
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okio.IOException
import retrofit2.Call
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

class PipApiImpl : PipApi {
    private val BASE_URL = BuildConfig.ANDROID_PIP_SERVER_IP

    private val service: Service by lazy {
        val retroFit =
            Retrofit.Builder().addConverterFactory(getJsonFactory()).baseUrl(BASE_URL).build()
        retroFit.create(Service::class.java)
    }

    override suspend fun getCarpark(id: String): Carpark? =
        service.getCarpark(id).awaitResponse().body()

    override suspend fun getCarparkAvailability(
        id: String,
        origin: Coordinate?,
        full: Boolean
    ): CarparkAvailability? =
        service.getCarparkAvailability(id, origin?.asIso6709(), full).awaitResponse().body()

    override suspend fun queryNearby(
        location: Coordinate,
        origin: Coordinate?,
        searchRadius: Int,
        full: Boolean
    ): IOFlow<CarparkAvailability> {
        val response = service.queryNearby(location.asIso6709(), origin?.asIso6709(), searchRadius, full, 1)
        val size = response.found
        val totalPages = response.numPages
        val oresults = response.results

        return IOFlow(size, flow {
            var page = 1
            var results = oresults
            do {
                if (page > 1) results =
                    service.queryNearby(location.asIso6709(), origin?.asIso6709(), searchRadius, full, page).results
                page += 1

                for (r in results) emit(r)
            } while (page <= totalPages) // Request the next page of results
        })
    }



    override suspend fun ping(): Boolean = try {
        service.ping().result
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }

    private fun getJsonFactory(): Converter.Factory {
        return Json {
            isLenient = true; ignoreUnknownKeys = true; allowStructuredMapKeys = true; prettyPrint =
            true; coerceInputValues = true
        }.asConverterFactory(
            "application/json".toMediaTypeOrNull()!!
        )
    }

    /**
     * We make an inner class to access instance variables from the outer scope
     * This works because inner classes are not static, they are instances
     */
    private interface Service {
        @GET("/api/v1/carpark/{id}")
        suspend fun getCarpark(
            @Path("id") id: String
        ): Call<Carpark>

        @POST("/api/v1/carpark/{id}")
        suspend fun getCarparkAvailability(
            @Path("id") id: String,
            @Query("origin") origin: String? = null,
            @Query("full") full: Boolean = false
        ): Call<CarparkAvailability>

        @POST("/api/v1/carpark/nearby")
        suspend fun queryNearby(
            @Query("location") location: String,
            @Query("origin") origin: String?,
            @Query("searchRadius") searchRadius: Int = 500,
            @Query("full") full: Boolean = false,
            @Query("page") page: Int = 1
        ): QueryNearbyResponse

        @GET("/")
        suspend fun ping(): Dummy
    }
}

@Serializable
data class Dummy(val result: Boolean)