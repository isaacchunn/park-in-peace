package ntu26.ss.parkinpeace.server.api

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.slf4j.LoggerFactory
import retrofit2.Converter
import retrofit2.HttpException

private val defaultJsonSerializer = Json {
    isLenient = true
    ignoreUnknownKeys = true
    allowStructuredMapKeys = true
    prettyPrint = true
    coerceInputValues = true
}

fun getJsonConverterFactory(): Converter.Factory {
    return defaultJsonSerializer.asConverterFactory(
        "application/json".toMediaTypeOrNull()!!
    )
}

inline fun <T> Any.guarded(tag: String = "", block: () -> T): T {
    try {
        return block()
    } catch (e: HttpException) {
        val wrap = ApiError("unable to perform api call ($tag)", e)
        LoggerFactory.getLogger(this::class.java).error("unable to perform api call ($tag)", wrap)
        throw wrap
    }
}