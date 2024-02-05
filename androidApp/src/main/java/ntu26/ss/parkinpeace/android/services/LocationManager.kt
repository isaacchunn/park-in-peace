package ntu26.ss.parkinpeace.android.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.plus
import ntu26.ss.parkinpeace.models.Coordinate

class LocationManager(
    scope: CoroutineScope,
    var resolver: QueryResolver = LocationService.getDefaultQueryResolver(scope + Dispatchers.IO),
    var estimatorStrategy: DistanceEstimatorStrategy = LocationService.DEFAULT_ESTIMATOR_STRATEGY
) : LocationService {

    // val storedLocations: List<*> = TODO()
    // val history: List<*> = TODO()

    override suspend fun resolve(query: String) = resolver.resolve(query)

    override suspend fun getCurrentCoordinates(): Coordinate {
        TODO("Not yet implemented") // https://developer.android.com/training/location
    }

    override suspend fun estimateDistance(from: Coordinate, to: Coordinate): Double =
        estimatorStrategy.estimateDistance(from, to)
}