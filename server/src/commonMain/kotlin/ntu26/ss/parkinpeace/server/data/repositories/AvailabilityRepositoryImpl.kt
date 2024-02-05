package ntu26.ss.parkinpeace.server.data.repositories

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.*
import ntu26.ss.parkinpeace.server.data.db.AvailabilityDao
import ntu26.ss.parkinpeace.server.data.external.*
import ntu26.ss.parkinpeace.server.models.Availability
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ulid.ULID
import kotlin.time.Duration.Companion.hours

class AvailabilityRepositoryImpl(
    private val carparkRepository: CarparkRepository,
    private val availabilityDao: AvailabilityDao,
    private val clock: Clock = Clock.System,
) : AvailabilityRepository {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)
    private var _hookDatagovCapacityFix = Instant.DISTANT_PAST
    private val _hookDatagovCapacityRefreshDuration = 24.hours

    override suspend fun get(carpark: String?, before: Instant): List<Availability> {
        carpark ?: return listOf()
        return availabilityDao.read(carpark, before)
    }

    override suspend fun observe(stream: Flow<List<ExternalAvailabilitySource>>) {
        withContext(Dispatchers.Default) {
            stream.collect {
                supervisorScope {
                    launch { onAvailabilityRefreshed(it) }
                    launch { hookDatagovCapacityFix(it) }
                }
            }
        }
    }

    private suspend fun onAvailabilityRefreshed(incoming: List<ExternalAvailabilitySource>) {
        coroutineScope {
            try {
                val now = clock.now().truncated()
                val ids = incoming.map { carparkRepository.resolve(it.asRef()) }
                val (resolved, unresolved) = (incoming zip ids).partition { it.second != null }
                val commit = resolved.flatMap { (uav, id) -> uav.toAvailability(id!!, now) }

                withContext(Dispatchers.IO) {
                    availabilityDao.create(commit)
                }
                logger.debug("onAvailabilityRefreshed: Vacancies in=${incoming.size} resolved=${resolved.size} dropped=${unresolved.size}")
            } catch (e: Exception) {
                logger.error("Unknown error while performing onAvailabilityRefreshed", e)
                throw e
            }
        }
    }

    private suspend fun hookDatagovCapacityFix(incoming: List<ExternalAvailabilitySource>) {
        val now = clock.now()
        if (now - _hookDatagovCapacityFix > _hookDatagovCapacityRefreshDuration) {
            val work = incoming.filterIsInstance<ExternalAvailabilitySource.Datagov>()
                .map { carparkRepository.resolve(it.value.ref) to it }
                .filter { it.first != null }

            if (work.isNotEmpty()) {
                for ((id, i) in work) {
                    carparkRepository.updateCapacity(id!!, i.value.vehicleType, i.value.capacity)
                }
                logger.debug("hookDatagovCapacityFix: modified ${work.size} lots, next update in ${_hookDatagovCapacityRefreshDuration.inWholeHours}h")
                _hookDatagovCapacityFix = now
            }
        }
    }
}

private fun UraAvailability.toAvailability(id: ULID, asof: Instant): List<Availability> =
    availabilities.map { (vehType, av) ->
        Availability(
            carparkId = id,
            vehicleType = vehType,
            availability = av,
            asof = asof
        )
    }

private fun LtaCarparkAvailability.toAvailability(id: ULID, asof: Instant): List<Availability> =
    listOf(
        Availability(
            carparkId = id,
            availability = availability,
            asof = asof,
            vehicleType = lotType
        )
    )

private fun DatagovAvailability.toAvailability(id: ULID, asof: Instant): List<Availability> =
    listOf(
        Availability(
            carparkId = id,
            availability = availability,
            asof = asof,
            vehicleType = vehicleType
        )
    )

private fun ExternalAvailabilitySource.toAvailability(id: ULID, asof: Instant): List<Availability> =
    when (this) {
        is ExternalAvailabilitySource.Lta -> this.value.toAvailability(id, asof)
        is ExternalAvailabilitySource.Ura -> this.value.toAvailability(id, asof)
        is ExternalAvailabilitySource.Datagov -> this.value.toAvailability(id, asof)
    }

private fun Instant.truncated(): Instant = this.toLocalDateTime(TimeZone.UTC).let {
    LocalDateTime(it.date, LocalTime(it.hour, it.minute, 0, 0))
}.toInstant(TimeZone.UTC)
