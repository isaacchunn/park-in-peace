package ntu26.ss.parkinpeace.server.services

import net.qxcg.svy21.SVY21
import ntu26.ss.parkinpeace.models.Coordinate
import ntu26.ss.parkinpeace.models.of
import kotlin.math.*
import kotlin.time.Duration

interface LocationService {
    interface Parameters {
        val destination: Coordinate
        val distance: Int
        val duration: Duration
    }

    suspend fun estimateTravelParameters(
        origin: Coordinate, dest: List<Coordinate>
    ): List<Parameters>

    suspend fun lookupAddress(epsg3414: String): List<String>
    suspend fun lookupAddress(epsg4326: Coordinate): List<String>

    suspend fun convertEpsg3414to4326(epsg3414: String): Coordinate
}

suspend fun LocationService.estimateTravelParameters(
    origin: Coordinate, dest: Coordinate
): LocationService.Parameters = estimateTravelParameters(origin, listOf(dest)).first()

/**
 * Computes the distance between two points. This does not compute the route distance.
 *
 * [Formula](https://community.fabric.microsoft.com/t5/Desktop/How-to-calculate-lat-long-distance/m-p/3455229/highlight/true#M1145425)
 *
 * @see ntu26.ss.parkinpeace.server.api.mapbox.MapBoxApi.travel
 * @see LocationService.estimateTravelParameters
 */
fun naiveStraightLineDistance(from: Coordinate, to: Coordinate): Int {
    val rads = Math::toRadians
    val r = 6371 * 1000 // distance in meters
    val lat1 = rads(from.latitude.toDouble())
    val lon1 = rads(from.longitude.toDouble())
    val lat2 = rads(to.latitude.toDouble())
    val lon2 = rads(to.longitude.toDouble())
    return (r * acos(sin(lat1) * sin(lat2) + cos(lat1) * cos(lat2) * cos(lon2 - lon1))).toInt()
}

/**
 * Converts EPSG:3414 to EPSG:4326 using SVY21 library.
 *
 * Note that this is likely less accurate than [ntu26.ss.parkinpeace.server.api.onemap.OneMapApi].
 * It is estimated to be accurate up to 5-6 decimal points.
 *
 * @see ntu26.ss.parkinpeace.server.api.onemap.OneMapApi.convertEpsg3414To4326
 * @see LocationService.convertEpsg3414to4326
 */
fun offlineEpsg3414toEpsg4326(epsg3414: String): Coordinate {
    val xy = epsg3414.split(',')
    return SVY21.computeLatLon(xy[1].toDouble(), xy[0].toDouble()).let {
        Coordinate.of(it.latitude, it.longitude)
    }
}