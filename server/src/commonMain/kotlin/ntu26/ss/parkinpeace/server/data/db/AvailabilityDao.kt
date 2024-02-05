package ntu26.ss.parkinpeace.server.data.db

import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Instant
import ntu26.ss.parkinpeace.decodeEnum
import ntu26.ss.parkinpeace.getSerialName
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import ulid.ULID
import ntu26.ss.parkinpeace.server.models.Availability as AvailabilityModel

class AvailabilityDao(private val database: Database) {
    object Availability : Table("Availability") {
        val carparkId = char("carpark_id", 26)
        val vehicleType = varchar("feature", 255)
        val asof = timestamp("asof")
        val availability = integer("availability")

        override val primaryKey = PrimaryKey(carparkId, vehicleType, asof)

        init {
            foreignKey(carparkId to RawCarparkDao.Carparks.id)
        }
    }

    init {
        transaction(database) {
            SchemaUtils.create(Availability)
        }
    }

    internal object INTERNAL {
        fun createAvailability(model: AvailabilityModel) = Availability.replace {
            it[carparkId] = model.carparkId.toString()
            it[vehicleType] = model.vehicleType.getSerialName()
            it[asof] = model.asof
            it[availability] = model.availability
        }

        /**
         * Guaranteed to be consistent.
         *
         * i.e. won't be CAR from 10PM, MOTORCYCLE from 9.58PM, etc.
         *
         * Will fetch only lots with the same asof.
         */
        fun readAvailabilityBefore(
            carpark: String,
            before: Instant
        ): List<AvailabilityModel> {
            val latest = Availability.select { Availability.asof lessEq before }
                .orderBy(Availability.asof, SortOrder.DESC).limit(1).singleOrNull()
                ?.let { it[Availability.asof] } ?: return listOf() // table is empty

            return Availability.select { (Availability.carparkId eq carpark).and(Availability.asof eq latest) }
                .map(INTERNAL::decodeAsAvailability)
        }

        fun decodeAsAvailability(row: ResultRow): AvailabilityModel = with(Availability) {
            AvailabilityModel(
                carparkId = ULID.parseULID(row[carparkId]),
                vehicleType = decodeEnum(row[vehicleType]),
                asof = row[asof],
                availability = row[availability]
            )
        }
    }

    suspend fun read(carpark: String, before: Instant): List<AvailabilityModel> =
        dbQuery { INTERNAL.readAvailabilityBefore(carpark, before) }

    suspend fun create(list: List<AvailabilityModel>) = dbQuery {
        for (availability in list) INTERNAL.createAvailability(availability)
    }

    private suspend fun <T> dbQuery(block: suspend Transaction.() -> T): T =
        newSuspendedTransaction(Dispatchers.IO, database, statement = block)
}