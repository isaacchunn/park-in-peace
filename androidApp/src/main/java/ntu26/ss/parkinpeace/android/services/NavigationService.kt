package ntu26.ss.parkinpeace.android.services

import ntu26.ss.parkinpeace.IOFlow
import ntu26.ss.parkinpeace.android.BuildConfig
import ntu26.ss.parkinpeace.android.models.CarparkAvailability
import ntu26.ss.parkinpeace.models.Coordinate
import ntu26.ss.parkinpeace.models.VehicleType

interface NavigationService {
    val carparkID: String
    val authenticated: Boolean

    data class QueryOptions(
        val searchRadius: Int,
        val limit: Int,
        val vehType: VehicleType,
        val computeDistance: Boolean,
        val computeTravelTime: Boolean,
        val computeCarparkDetails: Boolean
    )

    suspend fun subscribe(carparkId: String): Boolean
    suspend fun unsubscribe()
    suspend fun queryNearby(
        origin: Coordinate,
        destination: Coordinate,
        options: QueryOptions
    ): IOFlow<CarparkAvailability>

    suspend fun warnCarparkFull(carparkId: String)
    suspend fun refreshCarparkInfo(carparkId: String): CarparkAvailability?

    companion object {
        private val pip = NavigationWithPipManager()
        private val backup = NavigationManager()
        val single: NavigationService
            get() {
                return when {
                    BuildConfig.DEBUG && pip.authenticated -> pip
                    else -> backup
                }
            }
    }
}