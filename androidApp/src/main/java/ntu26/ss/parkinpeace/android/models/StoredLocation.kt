package ntu26.ss.parkinpeace.android.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import ntu26.ss.parkinpeace.models.Coordinate
import ntu26.ss.parkinpeace.models.Location

@Entity
data class StoredLocation(
    val tag: String,
    override val name: String,
    override val address: String,
    override val epsg4326: Coordinate,
    @PrimaryKey(autoGenerate = true)
    val id: Int =0,
) : Location

class CoordinateConverter {
    @TypeConverter
    fun fromCoordinate(coordinate: Coordinate): String {
        // Convert Coordinate to a String representation that can be stored in the database
        // You might need to adjust this based on the structure of your Coordinate class
        return "${coordinate.latitude},${coordinate.longitude}"
    }

    @TypeConverter
    fun toCoordinate(coordinateString: String): Coordinate {
        // Convert the stored String back to a Coordinate object
        // You might need to adjust this based on the structure of your Coordinate class
        val parts = coordinateString.split(",")
        val latitude = parts[0].toBigDecimal()
        val longitude = parts[1].toBigDecimal()
        return Coordinate.of(latitude, longitude)
    }
}