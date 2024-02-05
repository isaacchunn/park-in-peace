package ntu26.ss.parkinpeace.server.data.repositories

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ntu26.ss.parkinpeace.IOFlow
import ntu26.ss.parkinpeace.models.Coordinate
import ntu26.ss.parkinpeace.models.Lot
import ntu26.ss.parkinpeace.models.VehicleType
import ntu26.ss.parkinpeace.server.data.*
import ntu26.ss.parkinpeace.server.data.db.RawCarparkDao
import ntu26.ss.parkinpeace.server.data.external.ExternalCarparkSource
import ntu26.ss.parkinpeace.server.data.external.asRef
import ntu26.ss.parkinpeace.server.models.Carpark
import ntu26.ss.parkinpeace.server.services.naiveStraightLineDistance
import ntu26.ss.parkinpeace.server.services.offlineEpsg3414toEpsg4326
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ulid.ULID
import kotlin.time.Duration.Companion.minutes

class CarparkRepositoryImpl(
    private val rawCarparkDao: RawCarparkDao,
    private val rawDbCarparkFactory: RawDbCarpark.Factory,
    private val resolver: CarparkResolver,
    private val clock: Clock = Clock.System,
) : CarparkRepository {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)
    private val carparks: MutableMap<ULID, RawDbCarpark> = mutableMapOf()
    private val refCarparks: MutableMap<String, RawDbCarpark> = mutableMapOf()

    override operator fun contains(carpark: String?): Boolean {
        carpark ?: return false
        val id = ULID.parseULID(carpark)
        return carparks[id]?.isActive ?: false
    }

    override suspend fun get(carpark: String?): Carpark? {
        carpark ?: return null
        val id = ULID.parseULID(carpark)
        val cp = carparks[id] ?: return null
        if (!cp.isActive) return null
        return when (resolver.isResolved(cp)) {
            true -> cp.toCarpark()
            false -> resolveAndUpdate(cp).toCarpark()
        }
    }

    override fun resolve(ref: Ref?): ULID? = refCarparks[ref?.normalized]?.id

    override fun filter(location: Coordinate, searchRadius: Int, e: Int): List<String> {
        return carparks.values.asSequence().filter { it.isActive }.map {
            val coord = when (resolver.isResolved(it)) {
                true -> it
                else -> it.offlineResolve()
            }.epsg4326!!
            naiveStraightLineDistance(location, coord) to it
        }.filter { it.first <= searchRadius + e }.sortedBy { it.first }
            .map { it.second.id.toString() }.toList()
    }

    override suspend fun filterAndGet(
        location: Coordinate, searchRadius: Int, e: Int
    ): IOFlow<Carpark> {
        return supervisorScope {
            withContext(Dispatchers.IO) {
                val lst = filter(location, searchRadius, e)
                IOFlow(lst.size, lst.asFlow().map { get(it)!! })
            }
        }
    }

    override suspend fun observe(stream: Flow<List<ExternalCarparkSource>>) {
        coroutineScope {
            withContext(Dispatchers.IO) { sync() }
            launch(Dispatchers.Default) {
                stream.collect { supervisorScope { launch { onCarparkRefreshed(it) } } }
            }
            monitor()
        }
    }

    override suspend fun updateCapacity(carpark: ULID, vehicleType: VehicleType, capacity: Int) {
        carparks[carpark]?.let {
            it.copy(lots = it.lots.map {
                when (it.vehicleType) {
                    vehicleType -> it.copy(capacity = capacity)
                    else -> it
                }
            })
        }?.let {
            rawCarparkDao.sync(it)
            updateStructures(it)
        }
    }

    private suspend fun onCarparkRefreshed(incoming: List<ExternalCarparkSource>) {
        coroutineScope {
            try {
                val now = clock.now()
                val ours = refCarparks
                val theirs = incoming.reject().associateBy { it.asRef().normalized }
                val combined = (ours.keys + theirs.keys).map { ours[it] to theirs[it] }
                val diffList = combined.map { compare(it.first, it.second) }.groupBy { it }
                    .mapValues { it.value.size }
                val diff = combined.filter {
                    compare(it.first, it.second) !in listOf(
                        Status.UNCHANGED,
                        Status.IGNORED
                    )
                }
                    .map {
                        merge(
                            it.first,
                            it.second,
                            rawDbCarparkFactory,
                            resolver::isResolved,
                            now
                        )
                    }

                withContext(Dispatchers.IO) {
                    rawCarparkDao.sync(diff)
                    updateStructures(diff)
                    logger.info("onCarparksRefreshed: Carparks synced. Total=${carparks.size} Changed=${diff.size} diffList=$diffList.")
                    logger.debug("onCarparksRefreshed: Random 10 IDs: ${
                        diff.shuffled().take(10).map { it.id }
                    }")
                }
            } catch (e: Exception) {
                logger.error("Unknown error while performing onAvailabilityRefreshed", e)
                throw e
            }
        }
    }

    private suspend fun resolveAndUpdate(carpark: RawDbCarpark): RawDbCarpark {
        return withContext(Dispatchers.IO) {
            val cp = resolver.resolve(carpark).let {
                when (it.address) {
                    null -> {
                        logger.warn("resolveAndUpdate: Unable to resolve address for id=${it.id} ref=${it.ref} name=${it.name} xy=${it.epsg3414}")
                        it.copy(address = "")
                    }

                    else -> it
                }
            }
            launch { rawCarparkDao.sync(cp, true) }
            updateStructures(cp)
            cp
        }
    }

    private fun updateStructures(carpark: RawDbCarpark) {
        refCarparks[carpark.ref] = carpark
        carparks[carpark.id] = carpark
    }

    private fun updateStructures(carparks: List<RawDbCarpark>) {
        refCarparks.putAll(carparks.associateBy { it.ref })
        this.carparks.putAll(carparks.associateBy { it.id })
    }

    private suspend fun sync() {
        updateStructures(rawCarparkDao.readAll())
    }

    private suspend fun monitor() {
        supervisorScope {
            delay(1.minutes)
            while (isActive) {
                launch(Dispatchers.IO) {
                    tryResolveCarparks()
                }
                delay(3.minutes)
            }
        }
    }

    private suspend fun tryResolveCarparks(limit: Int = 40) {
        val unresolved =
            carparks.values.asSequence().filter { it.isActive }.filter { !resolver.isResolved(it) }
                .shuffled().take(limit).toList().map { get(it.id.toString()) }
        logger.info("tryResolveCarparks: Resolved ${unresolved.size} carparks.")
    }
}

private fun List<ExternalCarparkSource>.reject(): List<ExternalCarparkSource> {
    return filter {
        when (it) {
            is ExternalCarparkSource.Lta -> true
            is ExternalCarparkSource.Ura -> it.value.epsg3414 != null
            is ExternalCarparkSource.Datagov -> true
        }
    }
}

private fun RawDbCarpark.toCarpark(): Carpark {
    require(isActive) { "Attempted to convert inactive carpark" }
    return Carpark(
        id = id,
        ref = ref,
        name = name,
        address = address ?: throw IllegalArgumentException("address must not be null"),
        epsg4326 = epsg4326 ?: throw IllegalArgumentException("epsg4326 must not be null"),
        lots = lots,
        features = features,
        hash = hash
    )
}

private enum class Status {
    NEW,
    UNCHANGED,
    CHANGED,
    DELETED, // not possible to detect atm
    IGNORED
}

private fun isIdentical(ours: RawDbCarpark, theirs: ExternalCarparkSource): Boolean =
    when (theirs) {
        is ExternalCarparkSource.Ura -> ours.name == theirs.value.name && ours.lots.sortedBy { it.toString() } == theirs.value.lots.sortedBy { it.toString() } && theirs.value.epsg3414 != null // we do not check epsg3414 as it is not stable
        is ExternalCarparkSource.Lta -> {
            when (theirs.value.ref) {
                is UraRef, is HdbRef -> ours.epsg4326 == theirs.value.epsg4326
                is LtaRef -> ours.name == theirs.value.name && ours.epsg4326 == theirs.value.epsg4326
            }
        }

        is ExternalCarparkSource.Datagov -> {
            when (theirs.value.ref) {
                is HdbRef -> ours.name == theirs.value.name && ours.lots.sortedBy { it.toString() } == theirs.value.lots.sortedBy { it.toString() } && ours.epsg3414 == theirs.value.epsg3414
                is UraRef, is LtaRef -> TODO("not yet supported")
            }
        }
    }

private fun compare(ours: RawDbCarpark?, theirs: ExternalCarparkSource?): Status {
    return when {
        ours == null && theirs != null -> Status.NEW
        ours != null && theirs == null -> Status.IGNORED
        ours != null && theirs != null -> if (isIdentical(
                ours, theirs
            )
        ) Status.UNCHANGED else Status.CHANGED

        else -> throw IllegalArgumentException("Both ours and theirs cannot be simultaneously null")
    }
}

private fun merge(
    ours: RawDbCarpark?,
    theirs: ExternalCarparkSource?,
    factory: RawDbCarpark.Factory,
    isResolved: (RawDbCarpark) -> Boolean,
    asof: Instant
): RawDbCarpark {
    return when (compare(ours, theirs)) {
        Status.NEW -> when (theirs) {
            is ExternalCarparkSource.Ura -> factory.create(
                ref = theirs.asRef().normalized,
                name = theirs.value.name,
                address = null,
                epsg4326 = null,
                epsg3414 = theirs.value.epsg3414!!,
                lots = theirs.value.lots,
                features = listOf()
            )

            is ExternalCarparkSource.Lta -> factory.create(
                ref = theirs.asRef().normalized,
                name = theirs.value.name,
                address = null,
                epsg4326 = theirs.value.epsg4326,
                epsg3414 = "N/A",
                lots = listOf(),
                features = listOf()
            )

            is ExternalCarparkSource.Datagov -> factory.create(
                ref = theirs.asRef().normalized,
                name = theirs.value.name,
                address = null,
                epsg4326 = null,
                epsg3414 = theirs.value.epsg3414,
                lots = theirs.value.lots,
                features = listOf()
            )

            null -> throw IllegalArgumentException()
        }

        Status.DELETED -> ours!!.copy(isActive = false, asof = asof)
        Status.UNCHANGED, Status.IGNORED -> ours!!
        Status.CHANGED -> when (theirs) {
            is ExternalCarparkSource.Ura -> ours!!.copy(
                name = theirs.value.name,
                epsg3414 = if (isResolved(ours)) ours.epsg3414 else theirs.value.epsg3414!!,
                lots = theirs.value.lots.sortedBy(Lot::toString),
                isActive = true,
                asof = asof
            )

            is ExternalCarparkSource.Lta -> when (theirs.value.ref) {
                is UraRef -> ours!!.copy(
                    epsg4326 = theirs.value.epsg4326,
                    isActive = true,
                    asof = asof
                )

                is LtaRef, is HdbRef -> ours!!.copy(
                    name = theirs.value.name,
                    epsg4326 = theirs.value.epsg4326,
                    isActive = true,
                    asof = asof
                )
            }

            is ExternalCarparkSource.Datagov -> when (theirs.value.ref) {
                is LtaRef, is UraRef -> TODO("not supported yet")
                is HdbRef -> ours!!.copy(
                    name = theirs.value.name,
                    epsg3414 = theirs.value.epsg3414,
                    lots = theirs.value.lots.sortedBy(Lot::toString),
                )
            }

            null -> throw IllegalArgumentException()
        }

    }
}

private fun RawDbCarpark.offlineResolve(): RawDbCarpark {
    return when (epsg4326) {
        null -> {
            val coordinate = offlineEpsg3414toEpsg4326(epsg3414)
            return copy(epsg4326 = coordinate)
        }

        else -> this
    }
}
