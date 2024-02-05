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
    version = 1
)
abstract class UserLocationsDB: RoomDatabase() {
    abstract val dao: UserLocationsDao
    companion object {
        private const val DATABASE_NAME = "user_locations_database"

        fun buildDatabase(context: Context): UserLocationsDB {
            return Room.databaseBuilder(
                context.applicationContext,
                UserLocationsDB::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}