package ntu26.ss.parkinpeace.server.data.db

import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import ntu26.ss.parkinpeace.models.Coordinate
import ntu26.ss.parkinpeace.server.data.RawDbCarpark
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import ulid.ULID

class RawCarparkDao(private val database: Database) {
    private val INTERNAL = Internal()

    internal object Carparks : Table("Carpark") {
        val id = char("id", 26)
        val ref = text("ref", eagerLoading = true)
        val name = text("name", eagerLoading = true)
        val address = text("address", eagerLoading = true).nullable()

        /**
         * Coordinates as represented by [EPSG:4326](https://www.onemap.gov.sg/apidocs/apidocs/#coordinateConverters)
         * We will not be manipulating coordinates manually, thus it's best left as a string.
         *
         * This coordinate system is used by Google Maps, Waze, OneMap, etc. (i.e. all international maps)
         * This is the default coordinates sent to the client so that the client can interact with external SDKs.
         */
        val epsg4326 = text("epsg4326", eagerLoading = true).nullable()

        /**
         * Coordinates as represented by [EPSG:3414](https://www.onemap.gov.sg/apidocs/apidocs/#coordinateConverters)
         * We will not be manipulating coordinates manually, thus it's best left as a string.
         *
         * This coordinate system is used by URA, OneMap, etc. (i.e. all government APIs within Singapore)
         * We only ingest this coordinate system because the URA API returns it.
         * It will be converted to EPSG:4326 before any use.
         */
        val epsg3414 = text("epsg3414", eagerLoading = true)

        val isActive = bool("is_active")

        val asof = datetime("asof")

        override val primaryKey = PrimaryKey(id)

        init {
            index(isUnique = false, ref)
            index(isUnique = false, isActive)
        }
    }

    init {
        transaction(database) {
            SchemaUtils.create(Carparks)
        }
    }

    internal inner class Internal {
        fun createCarparkOnly(
            carpark: RawDbCarpark, asof: Instant = carpark.asof
        ) = Carparks.insert {
            it[id] = carpark.id.toString()
            it[ref] = carpark.ref
            it[name] = carpark.name
            it[address] = carpark.address
            it[epsg4326] = carpark.epsg4326?.asIso6709()
            it[epsg3414] = carpark.epsg3414
            it[isActive] = carpark.isActive
            it[Carparks.asof] = asof.toLocalDateTime(TimeZone.UTC)
        }

        fun createCarpark(
            carpark: RawDbCarpark, asof: Instant = carpark.asof
        ) {
            val id = carpark.id.toString()
            createCarparkOnly(carpark, asof)
            FeaturesDao.INTERNAL.syncFeatures(id, carpark.features)
            RatesDao.INTERNAL.syncLots(id, carpark.lots)
            CarparkHashesDao.INTERNAL.replaceHash(id, carpark.hash)
        }

        fun readActiveCarpark(carpark: String): RawDbCarpark? {
            val feats = FeaturesDao.INTERNAL.readFeatures(carpark)
            val lots = RatesDao.INTERNAL.readLots(carpark)
            val hashT = CarparkHashesDao.CarparkHashes
            return Carparks.join(
                hashT,
                JoinType.LEFT,
                onColumn = Carparks.id,
                otherColumn = CarparkHashesDao.CarparkHashes.carparkId
            ).select { (Carparks.id eq carpark).and(Carparks.isActive) }.singleOrNull()?.let {
                with(Carparks) {
                    RawDbCarpark.Factory().fromDatabase(
                        id = ULID.parseULID(it[id]),
                        ref = it[ref],
                        name = it[name],
                        address = it[address],
                        epsg4326 = Coordinate.parseOrNull(it[epsg4326]),
                        epsg3414 = it[epsg3414],
                        features = feats,
                        lots = lots,
                        isActive = it[isActive],
                        asof = it[asof].toInstant(TimeZone.UTC),
                        hash = it[CarparkHashesDao.CarparkHashes.hash]
                    )
                }
            }
        }

        fun modifyCarpark(
            carpark: String, hash: String, asof: Instant, block: Carparks.(UpdateBuilder<*>) -> Unit
        ) {
            Carparks.replace {
                it[id] = carpark
                block(Carparks, it)
                it[Carparks.asof] = asof.toLocalDateTime(TimeZone.UTC)
            }
            CarparkHashesDao.INTERNAL.replaceHash(carpark, hash)
        }

        fun syncCarpark(
            carpark: RawDbCarpark,
            shallowSync: Boolean = false,
        ) {
            val id = carpark.id.toString()
            modifyCarpark(carpark = id, hash = carpark.hash, asof = carpark.asof) {
                it[ref] = carpark.ref
                it[name] = carpark.name
                it[address] = carpark.address
                it[epsg4326] = carpark.epsg4326?.asIso6709()
                it[epsg3414] = carpark.epsg3414
                it[isActive] = carpark.isActive
            }
            if (!shallowSync) {
                FeaturesDao.INTERNAL.syncFeatures(id, carpark.features)
                RatesDao.INTERNAL.syncLots(id, carpark.lots)
            }
        }

        fun existsCarpark(carpark: String): Boolean {
            return when (Carparks.select { (Carparks.id eq carpark).and(Carparks.isActive) }
                .singleOrNull()) {
                null -> false
                else -> true
            }
        }

        fun readAllActiveCarparks(): List<RawDbCarpark> =
            Carparks.select { Carparks.isActive.eq(true) }
                .map { readActiveCarpark(it[Carparks.id])!! }
    }

    suspend fun create(carpark: RawDbCarpark) = dbQuery { INTERNAL.createCarpark(carpark) }

    suspend fun read(carpark: String): RawDbCarpark? =
        dbQuery { INTERNAL.readActiveCarpark(carpark) }

    suspend fun readAll(): List<RawDbCarpark> = dbQuery { INTERNAL.readAllActiveCarparks() }

    suspend fun has(carpark: String): Boolean = dbQuery { INTERNAL.existsCarpark(carpark) }

    suspend fun sync(carpark: RawDbCarpark, shallowSync: Boolean = false) =
        dbQuery { INTERNAL.syncCarpark(carpark, shallowSync) }

    suspend fun sync(carparks: List<RawDbCarpark>, shallowSync: Boolean = false) = dbQuery {
        for (carpark in carparks) INTERNAL.syncCarpark(carpark, shallowSync)
    }

    private suspend fun <T> dbQuery(block: suspend Transaction.() -> T): T =
        newSuspendedTransaction(Dispatchers.IO, database, statement = block)

}
