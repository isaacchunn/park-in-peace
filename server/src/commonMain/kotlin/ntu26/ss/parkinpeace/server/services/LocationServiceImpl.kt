package ntu26.ss.parkinpeace.server.services

import ntu26.ss.parkinpeace.models.Coordinate
import ntu26.ss.parkinpeace.server.api.mapbox.MapBoxApi
import ntu26.ss.parkinpeace.server.api.onemap.OneMapApi

class LocationServiceImpl(private val mapBoxApi: MapBoxApi, private val oneMapApi: OneMapApi) :
    LocationService {

    override suspend fun estimateTravelParameters(
        origin: Coordinate,
        dest: List<Coordinate>
    ): List<LocationService.Parameters> = mapBoxApi.travel(origin, dest).destinations

    override suspend fun lookupAddress(epsg3414: String): List<String> =
        oneMapApi.lookupPossibleAddresses(epsg3414)

    override suspend fun lookupAddress(epsg4326: Coordinate): List<String> =
        oneMapApi.lookupPossibleAddresses(epsg4326)

    override suspend fun convertEpsg3414to4326(epsg3414: String): Coordinate =
        oneMapApi.convertEpsg3414To4326(epsg3414)
}