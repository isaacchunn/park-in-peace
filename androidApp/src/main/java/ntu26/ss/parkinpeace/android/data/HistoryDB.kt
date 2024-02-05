package ntu26.ss.parkinpeace.android.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ntu26.ss.parkinpeace.android.models.CoordinateConverter
import ntu26.ss.parkinpeace.android.models.StoredLocation

@TypeConverters(CoordinateConverter::class)
@Database(
    entities = [StoredLocation::class],
    version = 2
)
abstract class HistoryDB: RoomDatabase() {
    abstract val dao: VisitedLocationsDao
    companion object {
        private const val DATABASE_NAME = "visited_locations_database"

        fun buildDatabase(context: Context): HistoryDB {
            return Room.databaseBuilder(
                context.applicationContext,
                HistoryDB::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}