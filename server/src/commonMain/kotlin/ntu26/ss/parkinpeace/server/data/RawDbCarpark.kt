package ntu26.ss.parkinpeace.server.data

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ntu26.ss.parkinpeace.Hashable
import ntu26.ss.parkinpeace.computeSHA3
import ntu26.ss.parkinpeace.models.Coordinate
import ntu26.ss.parkinpeace.models.Feature
import ntu26.ss.parkinpeace.models.Lot
import ntu26.ss.parkinpeace.server.data.RawDbCarpark.Factory
import ulid.ULID

/**
 * This class is intentionally not serializable. This class should not be exposed to downstream.
 * To create an instance of this class, use [Factory.create]
 * @see Factory.create
 */
data class RawDbCarpark private constructor(
    val id: ULID,
    val ref: String,
    val name: String,
    val address: String?,
    val epsg4326: Coordinate?,
    val epsg3414: String,
    val lots: List<Lot>,
    val features: List<Feature>,
    val isActive: Boolean,
    val asof: Instant
) : Hashable {

    override val hash: String by lazy {
        computeSHA3(
            copy(
                lots = lots.sortedBy(Lot::toString),
                features = features.sorted(),
                asof = Instant.DISTANT_PAST
            ).toString() // Need to sort lists to ensure stable hashing
        )
    }

    class Factory(
        private val idGenerator: ULID.Factory = ULID.Factory(),
        private val clock: Clock = Clock.System
    ) {
        fun create(
            ref: String,
            name: String,
            address: String?,
            epsg4326: Coordinate?,
            epsg3414: String,
            lots: List<Lot>,
            features: List<Feature>,
            isActive: Boolean = true,
            asof: Instant = clock.now()
        ): RawDbCarpark {
            return RawDbCarpark(
                id = idGenerator.nextULID(),
                ref = ref,
                name = name,
                address = address,
                epsg4326 = epsg4326,
                epsg3414 = epsg3414,
                lots = lots.sortedBy(Lot::toString),
                features = features.sorted(),
                isActive = isActive,
                asof = asof,
            )
        }

        internal fun fromDatabase(
            id: ULID,
            ref: String,
            name: String,
            address: String?,
            epsg4326: Coordinate?,
            epsg3414: String,
            lots: List<Lot>,
            features: List<Feature>,
            isActive: Boolean,
            asof: Instant,
            hash: String
        ): RawDbCarpark {
            val cp = RawDbCarpark(
                id = id,
                ref = ref,
                name = name,
                address = address,
                epsg4326 = epsg4326,
                epsg3414 = epsg3414,
                lots = lots.sortedBy(Lot::toString),
                features = features.sorted(),
                isActive = isActive,
                asof = asof
            )
            assert(cp.hash == hash) { "Hash stored in DB differed from computed hash" }
            return cp
        }
    }
}

