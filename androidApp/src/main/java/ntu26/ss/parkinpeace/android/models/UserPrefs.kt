package ntu26.ss.parkinpeace.android.models

import ntu26.ss.parkinpeace.models.VehicleType

data class UserPrefs(
    val searchDistance:Double,
    val vehicleType: VehicleType,
    val fuelType: FuelType
)