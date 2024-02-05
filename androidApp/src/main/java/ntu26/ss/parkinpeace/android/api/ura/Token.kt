package ntu26.ss.parkinpeace.android.api.ura

import com.google.gson.annotations.SerializedName

data class Token(

	@field:SerializedName("Status")
	val status: String? = null,

	@field:SerializedName("Message")
	val message: String? = null,

	@field:SerializedName("Result")
	val token: String? = null
)
