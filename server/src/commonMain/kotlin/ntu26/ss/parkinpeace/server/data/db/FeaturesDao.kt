package ntu26.ss.parkinpeace.server.data.db

import kotlinx.coroutines.Dispatchers
import ntu26.ss.parkinpeace.decodeEnum
import ntu26.ss.parkinpeace.getSerialName
import ntu26.ss.parkinpeace.models.Feature
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class FeaturesDao(private val database: Database) {
    internal object Features : Table("Features") {
        val carparkId = char("carpark_id", 26)
        val feature = varchar("feature", 255)

        override val primaryKey = PrimaryKey(carparkId, feature)

        init {
            foreignKey(carparkId to RawCarparkDao.Carparks.id)
        }
    }

    init {
        transaction(database) {
            SchemaUtils.create(Features)
        }
    }

    internal object INTERNAL {
        fun createFeature(carpark: String, feature: Feature) = Features.insert {
            it[carparkId] = carpark
            it[Features.feature] = feature.getSerialName()
        }

        fun readFeatures(carpark: String): List<Feature> =
            Features.select { Features.carparkId eq carpark }.map(INTERNAL::decodeAsFeature)

        fun syncFeatures(carpark: String, features: List<Feature>) {
            deleteFeatures(carpark)
            for (feature in features) createFeature(carpark, feature)
        }

        fun deleteFeatures(carpark: String) = Features.deleteWhere { carparkId eq carpark }

        fun decodeAsFeature(row: ResultRow): Feature = decodeEnum(row[Features.feature])
    }

    suspend fun reads(carpark: String): List<Feature> = dbQuery { INTERNAL.readFeatures(carpark) }

    suspend fun sync(carpark: String, features: List<Feature>) =
        dbQuery { INTERNAL.syncFeatures(carpark, features) }

    suspend fun deletes(carpark: String) = sync(carpark, listOf())

    private suspend fun <T> dbQuery(block: suspend Transaction.() -> T): T =
        newSuspendedTransaction(Dispatchers.IO, database, statement = block)
}