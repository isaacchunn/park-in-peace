package ntu26.ss.parkinpeace.android.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity
data class UserLocation(

    val name: String,
    val lat: BigDecimal,

    @PrimaryKey(autoGenerate = true)
    val id: Int =0,
    )