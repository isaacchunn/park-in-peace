package ntu26.ss.parkinpeace.server.data.repositories

import kotlinx.coroutines.flow.Flow
import ntu26.ss.parkinpeace.IOFlow
import ntu26.ss.parkinpeace.models.Coordinate
import ntu26.ss.parkinpeace.models.VehicleType
import ntu26.ss.parkinpeace.server.data.Ref
import ntu26.ss.parkinpeace.server.data.external.ExternalCarparkSource
import ntu26.ss.parkinpeace.server.models.Carpark
import ulid.ULID

interface CarparkRepository {
    operator fun contains(carpark: String?): Boolean
    fun resolve(ref: Ref?): ULID?
    fun filter(location: Coordinate, searchRadius: Int, e: Int = DEFAULT_EPSILON): List<String>
    suspend fun observe(stream: Flow<List<ExternalCarparkSource>>)
    suspend fun get(carpark: String?): Carpark?
    suspend fun filterAndGet(
        location: Coordinate,
        searchRadius: Int,
        e: Int = DEFAULT_EPSILON
    ): IOFlow<Carpark>

    suspend fun updateCapacity(carpark: ULID, vehicleType: VehicleType, capacity: Int)

    companion object {
        const val DEFAULT_EPSILON: Int = 1
    }
}