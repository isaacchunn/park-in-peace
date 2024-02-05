package ntu26.ss.parkinpeace.server.api.datagovsg

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias RawCarparksResponse = List<RawCarparkResponse>

@Serializable
data class RawCarparkResponse(
    @SerialName("car_park_no") val carparkNo: String,
    val address: String,
    @SerialName("x_coord") val xCoord: Double,
    @SerialName("y_coord") val yCoord: Double,
    @SerialName("car_park_type") val carparkType: String,
    @SerialName("type_of_parking_system") val typeOfParkingSystem: String,
    @SerialName("short_term_parking") val shortTermParking: String,
    @SerialName("free_parking") val freeParking: String,
    @SerialName("night_parking") val nightParking: String,
    @SerialName("car_park_decks") val carparkDecks: Int,
    @SerialName("gantry_height") val gantryHeight: Double,
    @SerialName("car_park_basement") val carparkBasement: String
)