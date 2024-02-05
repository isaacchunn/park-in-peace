package ntu26.ss.parkinpeace.android.api.ura

import com.google.gson.annotations.SerializedName

data class AvailableLotsResponse(

	@field:SerializedName("Status")
	val status: String? = null,

	@field:SerializedName("Message")
	val message: String? = null,

	@field:SerializedName("Result")
	val result: List<LotsResult?>? = null
)

data class LotsResult(

    @field:SerializedName("carparkNo")
	val carparkNo: String? = null,

    @field:SerializedName("geometries")
	val geometries: List<LotLocation?>? = null,

    @field:SerializedName("lotsAvailable")
	val lotsAvailable: String? = null,

    @field:SerializedName("lotType")
	val lotType: String? = null
)

data class LotLocation(

	@field:SerializedName("coordinates")
	val coordinates: String? = null
)
