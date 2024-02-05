package ntu26.ss.parkinpeace.server.controllers

import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.routing
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.*
import kotlinx.serialization.Serializable
import ntu26.ss.parkinpeace.models.Coordinate
import ntu26.ss.parkinpeace.models.VehicleType
import ntu26.ss.parkinpeace.server.data.repositories.AvailabilityRepository
import ntu26.ss.parkinpeace.server.data.repositories.CarparkRepository
import ntu26.ss.parkinpeace.server.models.Carpark
import ntu26.ss.parkinpeace.server.models.CarparkAvailability
import ntu26.ss.parkinpeace.server.models.QueryNearbyResponse
import ntu26.ss.parkinpeace.server.services.LocationService
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import kotlin.math.ceil
import kotlin.math.min
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.measureTimedValue

private val MAX_ENTRIES_PER_PAGE = 10

@Serializable
@Resource("/api/v1/carpark")
class CarparkApiRoot {
    /**
     * GET.
     *
     * Get basic information about a car park, e.g. name, address, coordinates, pricing, etc.
     *
     * POST.
     *
     * Get realtime information about a car park, e.g. current vacancy and predicted vacancy etc
     *
     */
    @Resource("{id}")
    data class ById(
        val parent: CarparkApiRoot = CarparkApiRoot(),
        val id: String,
        /**
         * Starting location, used to compute travelling time, in ISO6709+EPSG:4326 format
         */
        val origin: Coordinate? = null,
        /**
         * Whether basic information should be included in the response.
         */
        val full: Boolean = false
    )

    /**
     * POST.
     *
     * Endpoint for retrieving car parks near [location].
     *
     * Only [location] is mandatory, the rest are optional.
     */
    @Resource("nearby")
    data class Nearby(
        val parent: CarparkApiRoot = CarparkApiRoot(),
        /**
         * Coordinates of the intended location to search for close car parks, in ISO6709+EPSG:4326 format
         */
        val location: Coordinate?,
        /**
         * Maximum tolerable distance to search, in metres
         */
        val searchRadius: Int? = 500,
        /**
         * Page to return
         */
        val page: Int? = 1,
        /**
         * Whether to include basic information of the car park, e.g. total capacity, price, features, etc.
         */
        val full: Boolean? = false,
        /**
         * Starting location of the user, before the user starts the trip, which will be used to compute travelling time
         */
        val origin: Coordinate? = null,
    )
}

fun CarparkController(app: Application) {
    val logger = LoggerFactory.getLogger("ntu26.ss.parkinpeace.server.controllers.CarparkController")
    app.routing {
        val carparkRepository: CarparkRepository by inject()
        val availabilityRepository: AvailabilityRepository by inject()
        val locationService: LocationService by inject()

        get<CarparkApiRoot.ById> {
            when (val cp = carparkRepository.get(it.id)) {
                null -> call.respond(HttpStatusCode.NotFound)
                else -> call.respond(cp)
            }
        }

        post<CarparkApiRoot.ById> {
            val origin = it.origin
            val carpark = carparkRepository.get(it.id)

            when (carpark) {
                null -> call.respond(HttpStatusCode.NotFound)
                else -> call.respond(
                    listOf(carpark).toCarparkAvailabilities(
                        origin = origin,
                        full = it.full,
                        locationService = locationService,
                        availabilityRepository = availabilityRepository
                    ).single()
                )
            }
        }

        post<CarparkApiRoot.Nearby> {
            val location = it.location
            val page = (it.page ?: 1).coerceAtLeast(1)
            val origin = it.origin
            val full = it.full ?: false
            val searchRadius = (it.searchRadius ?: 500).coerceIn(50..5000)
            when (location) {
                null -> call.respond(HttpStatusCode.BadRequest)
                else -> carparkRepository.filterAndGet(location, searchRadius).let { ioflow ->
                    val numPages = ceil(ioflow.size.toDouble() / MAX_ENTRIES_PER_PAGE).toInt().coerceAtLeast(1)
                    val truePage = min(page, numPages)
                    val discard = (truePage - 1) * MAX_ENTRIES_PER_PAGE
                    val (cp, t1) = measureTimedValue { ioflow.drop(discard).take(MAX_ENTRIES_PER_PAGE).toList() }
                    val (cpa, t2) = measureTimedValue {
                        cp.toCarparkAvailabilities(
                            origin = origin,
                            full = full,
                            locationService = locationService,
                            availabilityRepository = availabilityRepository
                        )
                    }

                    logger.trace("queryNearbyStats: items=${cp.size} total=${(t1 + t2).inWholeMilliseconds}ms filterAndGet(onemap.resolve)=${t1.inWholeMilliseconds}ms getCarparkAvailabilities(mapbox)=${t2.inWholeMilliseconds}ms")

                    QueryNearbyResponse(
                        page = truePage,
                        numPages = numPages,
                        found = ioflow.size,
                        results = cpa
                    ).let { r -> call.respond(r) }
                }
            }
        }
    }
}

private suspend fun getCarparkAvailability(
    carpark: Carpark,
    origin: Coordinate?,
    distance: Int?,
    travelTime: Duration?,
    full: Boolean,
    availabilityRepository: AvailabilityRepository
): CarparkAvailability {
    val prediction = availabilityRepository.vacancies(carpark.id.toString(), travelTime)
    return CarparkAvailability(
        id = carpark.id.toString(),
        info = if (full) carpark else null,
        origin = origin,
        distance = distance,
        travelTime = travelTime?.inWholeMinutes?.toInt(),
        lots = prediction.result,
        asof = prediction.asof
    )
}

private suspend fun getCarparkAvailabilities(
    carparks: List<Carpark>,
    origin: Coordinate?,
    full: Boolean,
    locationService: LocationService,
    availabilityRepository: AvailabilityRepository
): List<CarparkAvailability> {
    return when (origin) {
        null -> carparks.map { getCarparkAvailability(it, origin, null, null, full, availabilityRepository) }
        else -> {
            val params = locationService.estimateTravelParameters(origin, carparks.map { it.epsg4326 })
            (params zip carparks).map { (param, carpark) ->
                getCarparkAvailability(
                    carpark = carpark,
                    origin = origin,
                    distance = param.distance,
                    travelTime = param.duration,
                    full = full,
                    availabilityRepository = availabilityRepository
                )
            }
        }
    }
}

private suspend fun List<Carpark>.toCarparkAvailabilities(
    origin: Coordinate?,
    full: Boolean,
    locationService: LocationService,
    availabilityRepository: AvailabilityRepository
) = getCarparkAvailabilities(
    carparks = this,
    origin = origin,
    full = full,
    locationService = locationService,
    availabilityRepository = availabilityRepository
)

/**
 * Data type for return value of [vacancies]
 * @see vacancies
 */
private data class Prediction(
    val result: Map<VehicleType, CarparkAvailability.Inner>,
    val asof: Instant
)

/**
 * Fetches the latest vacancies and predicts the vacancy after [travelTime] by using the previous
 * day as reference.
 *
 * ---
 * **Example**
 *
 * trip started at 10pm, estimated to take 1 hour. Hence, will arrive at 11pm.
 *
 * server will return:
 * - current availability as of 10pm (if available or the most recent otherwise)
 * - predicted availability as of 11pm yesterday (most recent within [e] duration or -1)
 * ---
 *
 * NOTE: asof should be checked to see if the data returned was recent (within 1 hour) in case of server outage.
 * because server returns *most recent* data.
 *
 * @param e maximum tolerance for prediction data. If there is no entry within [e] duration
 * of the desired timing within the past day, prediction will be -1 to indicate prediction failure.
 */
private suspend fun AvailabilityRepository.vacancies(
    carpark: String,
    travelTime: Duration?,
    e: Duration = 2.hours,
    now: Instant = Clock.System.now(),
): Prediction {
    val prediction = vacancies(carpark, now)
    if (travelTime == null) return prediction

    val dayBefore = now.minus(DateTimePeriod(days = 1), TimeZone.UTC) + travelTime.remTime()
    val predicted = when {
        prediction.result.isNotEmpty() && prediction.asof > dayBefore -> {
            val t = get(carpark, dayBefore)
            val tasof = t.firstOrNull()?.asof
            when {
                tasof != null && (dayBefore - tasof) <= e -> t.associateBy { it.vehicleType }
                else -> mapOf()
            }
        }

        else -> mapOf()
    }

    val result = prediction.result.mapValues { (k, v) ->
        CarparkAvailability.Inner(v.current, predicted[k]?.availability ?: -1)
    }

    return prediction.copy(result = result)
}

private suspend fun AvailabilityRepository.vacancies(
    carpark: String,
    now: Instant = Clock.System.now()
): Prediction {
    val records = get(carpark, now)
    val current = records.associateBy { it.vehicleType }
    val asof = records.firstOrNull()?.asof

    val result = current.mapValues { (_, v) ->
        CarparkAvailability.Inner(v.availability, -1)
    }

    return Prediction(result, asof ?: now)
}

/**
 * Skips whole days and returns the part thereof.
 *
 * Examples
 * - 36 hours -> 12 hours
 * - 14 days 10 mins -> 10 mins
 *
 * @return remaining time. less than a day.
 */
private fun Duration.remTime() = if (inWholeDays >= 1) minus(inWholeDays.days) else this