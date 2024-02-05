package ntu26.ss.parkinpeace.android.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import ntu26.ss.parkinpeace.models.Coordinate

@Entity
data class VisitedLocations(

    val name: String,
    val lat: Double,
    val long: Double,

    @PrimaryKey(autoGenerate = true)
    val id: Int =0,
)