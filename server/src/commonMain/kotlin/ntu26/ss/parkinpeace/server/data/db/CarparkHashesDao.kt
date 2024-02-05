package ntu26.ss.parkinpeace.server.data.db

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.replace
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class CarparkHashesDao(private val database: Database) {
    internal object CarparkHashes : Table("CarparkHashes") {
        val carparkId = char("carpark_id", 26)
        val hash = text("hash", eagerLoading = true)

        override val primaryKey = PrimaryKey(carparkId)

        init {
            foreignKey(carparkId to RawCarparkDao.Carparks.id)
        }
    }

    init {
        transaction(database) {
            SchemaUtils.create(CarparkHashes)
        }
    }

    internal object INTERNAL {
        fun createHash(carpark: String, hash: String) = CarparkHashes.insert {
            it[carparkId] = carpark
            it[CarparkHashes.hash] = hash
        }

        fun readHash(carpark: String): String? =
            CarparkHashes.select { CarparkHashes.carparkId eq carpark }.map(INTERNAL::decodeAsHash)
                .singleOrNull()

        fun replaceHash(carpark: String, hash: String) = CarparkHashes.replace {
            it[carparkId] = carpark
            it[CarparkHashes.hash] = hash
        }

        fun decodeAsHash(row: ResultRow): String = row[CarparkHashes.hash]
    }

    private fun deleteHash(carpark: String) = CarparkHashes.deleteWhere { carparkId eq carpark }

    suspend fun read(carpark: String): String? = dbQuery { INTERNAL.readHash(carpark) }

    suspend fun sync(carpark: String, hash: String) = dbQuery {
        INTERNAL.replaceHash(
            carpark,
            hash
        )
    }

    suspend fun delete(carpark: String) = dbQuery { deleteHash(carpark) }

    private suspend fun <T> dbQuery(block: suspend Transaction.() -> T): T =
        newSuspendedTransaction(Dispatchers.IO, database, statement = block)


}