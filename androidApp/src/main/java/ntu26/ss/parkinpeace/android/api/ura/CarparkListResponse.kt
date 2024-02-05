package ntu26.ss.parkinpeace.android.api.ura

import com.google.gson.annotations.SerializedName

data class CarparkListResponse(

	@field:SerializedName("Status")
	val status: String? = null,

	@field:SerializedName("Message")
	val message: String? = null,

	@field:SerializedName("Result")
	val result: List<ListResult?>? = null
)
data class ListResult(

    @field:SerializedName("weekdayMin")
	val weekdayMin: String? = null,

    @field:SerializedName("weekdayRate")
	val weekdayRate: String? = null,

    @field:SerializedName("ppCode")
	val ppCode: String? = null,

    @field:SerializedName("parkingSystem")
	val parkingSystem: String? = null,

    @field:SerializedName("ppName")
	val ppName: String? = null,

    @field:SerializedName("vehCat")
	val vehCat: String? = null,

    @field:SerializedName("satdayMin")
	val satdayMin: String? = null,

    @field:SerializedName("satdayRate")
	val satdayRate: String? = null,

    @field:SerializedName("sunPHMin")
	val sunPHMin: String? = null,

    @field:SerializedName("sunPHRate")
	val sunPHRate: String? = null,

    @field:SerializedName("geometries")
	val geometries: List<LotLocation?>? = null,

    @field:SerializedName("startTime")
	val startTime: String? = null,

    @field:SerializedName("parkCapacity")
	val parkCapacity: Int? = null,

    @field:SerializedName("endTime")
	val endTime: String? = null
)
