package ntu26.ss.parkinpeace.server.api.lta

import ntu26.ss.parkinpeace.models.Coordinate
import ntu26.ss.parkinpeace.models.VehicleType
import ntu26.ss.parkinpeace.models.of
import ntu26.ss.parkinpeace.server.api.ApiError
import ntu26.ss.parkinpeace.server.data.HdbRef
import ntu26.ss.parkinpeace.server.data.LtaRef
import ntu26.ss.parkinpeace.server.data.Ref
import ntu26.ss.parkinpeace.server.data.UraRef
import ntu26.ss.parkinpeace.server.data.external.LtaCarparkAvailability

suspend fun LtaApi.getCarparksAndAvailabilities(): List<LtaCarparkAvailability> =
    getCarparkAvailability().map {
        LtaCarparkAvailability(
            ref = it.agency.asRefOrThrow(it.carparkId),
            name = it.development,
            epsg4326 = it.location.asCoordinateOrThrow(),
            lotType = it.lotType.asVehicleTypeOrThrow(),
            availability = it.availableLots
        )
    }

private fun String?.asVehicleTypeOrThrow() =
    LtaI18n.parseVehicleTypeOrNull(this) ?: throw ApiError("Unable to parse $this as VehicleType")

private fun String?.asRefOrThrow(id: String?) =
    LtaI18n.parseRefOrNull(this, id)
        ?: throw ApiError("Unable to parse agency=$this carparkId=$id as Ref")

private fun String?.asCoordinateOrThrow() =
    LtaI18n.parseCoordinateOrNull(this) ?: throw ApiError("Unable to parse $this as Coordinate")

/**
 * Class with knowledge of LTA-specific formats
 */
private object LtaI18n {

    fun parseVehicleTypeOrNull(string: String?): VehicleType? = when (string) {
        "C" -> VehicleType.CAR
        "Y" -> VehicleType.MOTORCYCLE
        "H" -> VehicleType.HEAVY_VEHICLE
        else -> null
    }

    fun parseRefOrNull(agency: String?, id: String?): Ref? = when {
        agency != null && id != null -> when (agency) {
            "LTA" -> LtaRef(id)
            "URA" -> UraRef(id)
            "HDB" -> HdbRef(id)
            else -> null
        }

        else -> null
    }

    fun parseCoordinateOrNull(string: String?): Coordinate? {
        string ?: return null
        val (lat, lon) = string.split(" ")
        return Coordinate.of(lat, lon)
    }
}