package ntu26.ss.parkinpeace.server.data.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ntu26.ss.parkinpeace.server.data.external.ExternalAvailabilitySource
import ntu26.ss.parkinpeace.server.models.Availability

interface AvailabilityRepository {
    suspend fun observe(stream: Flow<List<ExternalAvailabilitySource>>)
    suspend fun get(carpark: String?, before: Instant): List<Availability>
}