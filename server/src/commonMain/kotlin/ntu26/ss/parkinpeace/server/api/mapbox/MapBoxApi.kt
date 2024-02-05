package ntu26.ss.parkinpeace.server.api.mapbox

import ntu26.ss.parkinpeace.models.Coordinate
import ntu26.ss.parkinpeace.server.services.LocationService

interface MapBoxApi {
    interface TravelResult {
        val origin: Coordinate
        val destinations: List<LocationService.Parameters>
    }

    suspend fun travel(origin: Coordinate, destinations: List<Coordinate>): TravelResult
}