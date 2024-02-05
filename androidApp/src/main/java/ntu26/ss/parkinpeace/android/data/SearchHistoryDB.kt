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
abstract class SearchHistoryDB: RoomDatabase() {
    abstract val dao: SearchHistoryDao

    companion object {
        private const val DATABASE_NAME = "search_locations_database"

        fun buildDatabase(context: Context): SearchHistoryDB {
            return Room.databaseBuilder(
                context.applicationContext,
                SearchHistoryDB::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}