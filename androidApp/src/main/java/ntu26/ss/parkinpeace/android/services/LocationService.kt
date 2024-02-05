package ntu26.ss.parkinpeace.android.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import ntu26.ss.parkinpeace.IOFlow
import ntu26.ss.parkinpeace.android.api.onemap.OneMapApi
import ntu26.ss.parkinpeace.android.api.onemap.OneMapApiImpl
import ntu26.ss.parkinpeace.models.Coordinate
import ntu26.ss.parkinpeace.models.Location
import kotlin.math.*

fun interface DistanceEstimatorStrategy {
    /**
     * @return distance in metres between [from] and [to]
     */
    suspend fun estimateDistance(from: Coordinate, to: Coordinate): Double
}

fun interface Locator {
    suspend fun getCurrentCoordinates(): Coordinate
}

fun interface QueryResolver {
    suspend fun resolve(query: String): IOFlow<Location>
}

interface LocationService : QueryResolver, Locator, DistanceEstimatorStrategy {
    companion object {
        /**
         * Computes the distance between two points. This does not compute the route distance.
         *
         * [Formula](https://community.fabric.microsoft.com/t5/Desktop/How-to-calculate-lat-long-distance/m-p/3455229/highlight/true#M1145425)
         *
         * @see ntu26.ss.parkinpeace.server.api.mapbox.MapBoxApi.travel
         * @see LocationService.estimateTravelParameters
         */
        val NAIVE_EUCLIDEAN_ESTIMATOR = DistanceEstimatorStrategy { from, to ->
            val rads = Math::toRadians
            val r = 6371 * 1000 // distance in meters
            val lat1 = rads(from.latitude.toDouble())
            val lon1 = rads(from.longitude.toDouble())
            val lat2 = rads(to.latitude.toDouble())
            val lon2 = rads(to.longitude.toDouble())
            r * acos(sin(lat1) * sin(lat2) + cos(lat1) * cos(lat2) * cos(lon2 - lon1))
        }

        val DEFAULT_ESTIMATOR_STRATEGY: DistanceEstimatorStrategy = NAIVE_EUCLIDEAN_ESTIMATOR

        fun getDefaultQueryResolver(scope: CoroutineScope): QueryResolver = object : QueryResolver {
            val api: OneMapApi = OneMapApiImpl()
            override suspend fun resolve(query: String): IOFlow<Location> =
                withContext(scope.coroutineContext) { api.search(query) }
        }
    }
}