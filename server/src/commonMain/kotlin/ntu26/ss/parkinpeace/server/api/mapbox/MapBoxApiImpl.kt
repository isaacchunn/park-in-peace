package ntu26.ss.parkinpeace.server.api.mapbox

import ntu26.ss.parkinpeace.models.Coordinate
import ntu26.ss.parkinpeace.server.api.getJsonConverterFactory
import ntu26.ss.parkinpeace.server.api.guarded
import ntu26.ss.parkinpeace.server.services.LocationService
import ntu26.ss.parkinpeace.server.services.Secrets
import org.slf4j.LoggerFactory
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class MapBoxApiImpl : MapBoxApi {
    private val BASE_URL = "https://api.mapbox.com"
    private val ACCESS_TOKEN: String get() = Secrets.MAPBOX_TOKEN

    private val logger = LoggerFactory.getLogger(this::class.java)

    private val service: Service by lazy {
        val retroFit =
            Retrofit.Builder().addConverterFactory(getJsonConverterFactory()).baseUrl(BASE_URL)
                .build()
        retroFit.create(Service::class.java)
    }

    override suspend fun travel(
        origin: Coordinate, destinations: List<Coordinate>
    ): MapBoxApi.TravelResult {
        if (destinations.isEmpty()) return TravelResultImpl(origin, listOf())

        val coordinates =
            (listOf(origin) + destinations).joinToString(";") { MapBoxI18n.format(it) }
        val response = this@MapBoxApiImpl.guarded {
            logger.trace("calling travel with params: coordinates=$coordinates")
            service.directionsMatrix(
                accessToken = ACCESS_TOKEN, coordinates = coordinates, sources = "0"
            )
        }
        return TravelResultImpl(origin,
            response.distances[0].zip(response.durations[0]).drop(1)
                .mapIndexed { index, (dist, dur) ->
                    TravelResultInnerImpl(
                        destination = destinations[index],
                        distance = MapBoxI18n.parseDistance(dist),
                        duration = MapBoxI18n.parseDuration(dur)
                    )
                })
    }

    private interface Service {
        @GET("directions-matrix/v1/mapbox/driving/{coordinates}")
        suspend fun directionsMatrix(
            // @Path("profile") profile: String = "mapbox/driving",
            @Path("coordinates") coordinates: String,
            @Query("annotations") annotations: String = "duration,distance",
            @Query("sources") sources: String = "0",
            @Query("access_token") accessToken: String
        ): RawDirectionsMatrixResponse
    }
}

private object MapBoxI18n {
    /**
     * Duration is in seconds
     */
    fun parseDuration(duration: Double): Duration = duration.seconds

    /**
     * Distance is in meters
     */
    fun parseDistance(distance: Double): Int = distance.roundToInt()

    fun format(coordinate: Coordinate): String = "${coordinate.longitude},${coordinate.latitude}"
}

private data class TravelResultImpl(
    override val origin: Coordinate,
    override val destinations: List<LocationService.Parameters>
) : MapBoxApi.TravelResult

private data class TravelResultInnerImpl(
    override val destination: Coordinate,
    override val distance: Int,
    override val duration: Duration
) : LocationService.Parameters