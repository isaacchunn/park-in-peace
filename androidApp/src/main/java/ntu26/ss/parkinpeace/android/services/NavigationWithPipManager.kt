package ntu26.ss.parkinpeace.android.services

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ntu26.ss.parkinpeace.IOFlow
import ntu26.ss.parkinpeace.android.api.pip.PipApi
import ntu26.ss.parkinpeace.android.api.pip.PipApiImpl
import ntu26.ss.parkinpeace.android.models.CarparkAvailability
import ntu26.ss.parkinpeace.models.Coordinate

class NavigationWithPipManager : NavigationService {
    private val api: PipApi = PipApiImpl()

    override val carparkID: String
        get() = TODO("Not yet implemented")

    override var authenticated: Boolean = false
        private set

    init {
        // TODO fix
        GlobalScope.launch(Dispatchers.IO) {
            authenticated = api.ping()
            if (authenticated) Log.d(this@NavigationWithPipManager::class.simpleName, "pip ready")
        }
    }


    override suspend fun subscribe(carparkId: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun unsubscribe() {
        TODO("Not yet implemented")
    }

    override suspend fun queryNearby(
        origin: Coordinate,
        destination: Coordinate,
        options: NavigationService.QueryOptions
    ): IOFlow<CarparkAvailability> = api.queryNearby(
        location = destination,
        origin = origin,
        searchRadius = options.searchRadius,
        full = true
    )

    override suspend fun warnCarparkFull(carparkId: String) {
        TODO("Not yet implemented")
    }

    // TODO refactor contract
    override suspend fun refreshCarparkInfo(carparkId: String): CarparkAvailability? =
        api.getCarparkAvailability(carparkId)
}