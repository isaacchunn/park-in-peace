package ntu26.ss.parkinpeace.server.api.onemap

import ntu26.ss.parkinpeace.models.Coordinate

interface OneMapApi {

    /**
     * Returns possible candidate Singapore addresses specified by [epsg3414]
     */
    suspend fun lookupPossibleAddresses(epsg3414: String): List<String>
    suspend fun lookupPossibleAddresses(epsg4326: Coordinate): List<String>

    suspend fun convertEpsg3414To4326(epsg3414: String): Coordinate
}
