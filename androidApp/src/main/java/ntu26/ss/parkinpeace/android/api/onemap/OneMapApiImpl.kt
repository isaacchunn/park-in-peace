package ntu26.ss.parkinpeace.android.api.onemap

import android.util.Log
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlinx.serialization.json.Json
import ntu26.ss.parkinpeace.IOFlow
import ntu26.ss.parkinpeace.models.Coordinate
import ntu26.ss.parkinpeace.models.Location
import ntu26.ss.parkinpeace.models.of
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

class OneMapApiImpl : OneMapApi {
    private val BASE_URL = "https://www.onemap.gov.sg/api/"

    private val service: Service by lazy {
        val retroFit =
            Retrofit.Builder().addConverterFactory(getJsonFactory()).baseUrl(BASE_URL).build()
        retroFit.create(Service::class.java)
    }

    private suspend fun request(query: String, pageNum: Int): RawSearchResponse {
        Log.d(
            this@OneMapApiImpl::class.simpleName,
            "OneMap::search query=$query, pageNum=$pageNum context=${currentCoroutineContext()}"
        )
        return service.search(query, pageNum = pageNum)
    }

    override suspend fun search(query: String): IOFlow<Location> {
        val response = request(query, pageNum = 1)
        val size = response.found
        val totalPages = response.totalNumPages
        val oresults = response.results

        return IOFlow(size, channelFlow<Location> {
            var page = 1
            var results = oresults
            do {
                if (page > 1) results = request(query, pageNum = page).results
                page += 1

                for (r in results) {
                    send(
                        OneMapApi.SearchResult(
                            name = r.searchVal,
                            address = r.address,
                            epsg4326 = Coordinate.of(r.latitude, r.longitude)
                        )
                    )
                }
            } while (page <= totalPages) // Request the next page of results
            awaitClose {}
        }.buffer(0))
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
        @GET("common/elastic/search")
        suspend fun search(
            @Query("searchVal") searchVal: String,
            @Query("returnGeom") returnGeom: String = "Y",
            @Query("getAddrDetails") getAddrDetails: String = "Y",
            @Query("pageNum") pageNum: Int = 1
        ): RawSearchResponse
    }
}