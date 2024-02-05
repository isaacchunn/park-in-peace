package ntu26.ss.parkinpeace.server.services

import ntu26.ss.parkinpeace.server.api.firebase.FirebaseApi
import ntu26.ss.parkinpeace.server.data.repositories.AvailabilityRepository
import ulid.ULID

interface SubscriptionManager {
    fun subscribe(token: String, carpark: ULID)
    fun unsubscribe(token: String)
    fun start()
}

class SubscriptionManagerImpl(
    private val firebaseApi: FirebaseApi,
    private val availabilityRepository: AvailabilityRepository
) : SubscriptionManager {
    private val subscriptions: Map<ULID, List<String>> = mapOf()

    override fun subscribe(token: String, carpark: ULID) {
        TODO("Not yet implemented")
    }

    override fun unsubscribe(token: String) {
        TODO("Not yet implemented")
    }

    override fun start() {
        TODO("Not yet implemented")
    }

    private suspend fun onAvailabilityRefresh(lst: Map<ULID, Int>) {

    }

    private suspend fun notifySubscribers() {}
}