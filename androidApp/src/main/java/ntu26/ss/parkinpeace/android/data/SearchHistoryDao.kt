package ntu26.ss.parkinpeace.android.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import ntu26.ss.parkinpeace.android.models.StoredLocation

@Dao
interface SearchHistoryDao {
    @Insert
    suspend fun insertLocation(location : StoredLocation)
    @Delete
    suspend fun deleteLocation(location: StoredLocation)
    @Query("SELECT * FROM storedlocation")
    suspend fun getLocations(): List<StoredLocation>

    @Query("DELETE FROM storedlocation")
    fun deleteAll()
}