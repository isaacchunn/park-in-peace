package ntu26.ss.parkinpeace.android.api.pip

import ntu26.ss.parkinpeace.IOFlow
import ntu26.ss.parkinpeace.android.models.Carpark
import ntu26.ss.parkinpeace.android.models.CarparkAvailability
import ntu26.ss.parkinpeace.models.Coordinate

interface PipApi {
    suspend fun getCarpark(id: String): Carpark?
    suspend fun getCarparkAvailability(
        id: String,
        origin: Coordinate? = null,
        full: Boolean = false
    ): CarparkAvailability?

    suspend fun queryNearby(
        location: Coordinate,
        origin: Coordinate? = null,
        searchRadius: Int = 500,
        full: Boolean = false
    ): IOFlow<CarparkAvailability>

    suspend fun ping(): Boolean
}