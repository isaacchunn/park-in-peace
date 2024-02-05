package ntu26.ss.parkinpeace.server.data.db

import kotlinx.coroutines.Dispatchers
import ntu26.ss.parkinpeace.decodeEnum
import ntu26.ss.parkinpeace.fromMinutesOfDay
import ntu26.ss.parkinpeace.getSerialName
import ntu26.ss.parkinpeace.models.Lot
import ntu26.ss.parkinpeace.toMinutesOfDay
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class RatesDao(private val database: Database) {
    internal object Rates : Table("Rates") {
        val carparkId = char("carpark_id", 26)
        val vehicleType = varchar("vehicle_type", 255)
        val chargeType = varchar("charge_type", 255)
        val startTime = integer("start_time")
        val endTime = integer("end_time")
        val rate = integer("rate")
        val minDuration = integer("min_duration")
        val capacity = integer("capacity")
        val system = varchar("system", 255)

        override val primaryKey =
            PrimaryKey(carparkId, vehicleType, chargeType, startTime, minDuration)

        init {
            index(isUnique = false, carparkId)
            index(isUnique = false, vehicleType)
            index(isUnique = false, chargeType)
            foreignKey(carparkId to RawCarparkDao.Carparks.id)
        }
    }

    init {
        transaction(database) {
            SchemaUtils.create(Rates)
        }
    }

    internal object INTERNAL {
        fun createLot(carpark: String, lot: Lot) = Rates.insert {
            it[carparkId] = carpark
            it[vehicleType] = lot.vehicleType.getSerialName()
            it[chargeType] = lot.chargeType.getSerialName()
            it[startTime] = lot.startTime.toMinutesOfDay()
            it[endTime] = lot.endTime.toMinutesOfDay()
            it[rate] = lot.rate
            it[minDuration] = lot.minDuration
            it[capacity] = lot.capacity
            it[system] = lot.system.getSerialName()
        }

        fun readLots(carpark: String): List<Lot> =
            Rates.select { Rates.carparkId eq carpark }.map(INTERNAL::decodeAsLot)

        fun deleteLots(carpark: String) = Rates.deleteWhere { carparkId eq carpark }

        fun syncLots(carpark: String, lots: List<Lot>) {
            assert(lots == lots.sortedBy(Lot::toString)) { "Integrity error. Lots must be sorted or else hash will be incorrect." }
            deleteLots(carpark)
            for (lot in lots) createLot(carpark, lot)
        }

        fun decodeAsLot(row: ResultRow): Lot = row.let {
            with(Rates) {
                Lot(
                    vehicleType = decodeEnum(it[vehicleType]),
                    chargeType = decodeEnum(it[chargeType]),
                    startTime = fromMinutesOfDay(it[startTime]),
                    endTime = fromMinutesOfDay(it[endTime]),
                    rate = it[rate],
                    minDuration = it[minDuration],
                    capacity = it[capacity],
                    system = decodeEnum(it[system])
                )
            }
        }
    }

    suspend fun reads(carpark: String): List<Lot> = dbQuery { INTERNAL.readLots(carpark) }

    suspend fun sync(carpark: String, lots: List<Lot>) = dbQuery {
        INTERNAL.syncLots(
            carpark,
            lots
        )
    }

    suspend fun deletes(carpark: String) = sync(carpark, listOf())

    private suspend fun <T> dbQuery(block: suspend Transaction.() -> T): T =
        newSuspendedTransaction(Dispatchers.IO, database, statement = block)
}