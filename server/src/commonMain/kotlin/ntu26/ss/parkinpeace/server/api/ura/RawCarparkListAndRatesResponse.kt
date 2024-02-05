package ntu26.ss.parkinpeace.server.api.ura

import kotlinx.serialization.Serializable

typealias RawCarparkListAndRatesResponse = RawUraApiResponse<List<RawCarparkListAndRates>>

@Serializable
data class RawCarparkListAndRates(
    val weekdayMin: String,
    val weekdayRate: String,
    val ppCode: String,
    val parkingSystem: String,
    val ppName: String,
    val vehCat: String,
    val satdayMin: String,
    val satdayRate: String,
    val sunPHMin: String,
    val sunPHRate: String,
    val geometries: List<RawGeometry>,
    val startTime: String,
    val parkCapacity: Int,
    val endTime: String,
    val remarks: String? = null
)
