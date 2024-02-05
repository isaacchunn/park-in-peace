package ntu26.ss.parkinpeace.server.api.onemap

import com.sletmoe.bucket4k.SuspendingBucket
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Refill
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import ntu26.ss.parkinpeace.models.Coordinate
import ntu26.ss.parkinpeace.models.of
import ntu26.ss.parkinpeace.server.api.getJsonConverterFactory
import ntu26.ss.parkinpeace.server.api.guarded
import ntu26.ss.parkinpeace.server.services.Secrets
import retrofit2.Retrofit
import retrofit2.http.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

class OneMapApiImpl(private val clock: Clock = Clock.System) : OneMapApi {
    private val CREDENTIALS = Credentials(Secrets.ONEMAP_USER, Secrets.ONEMAP_PASS)
    private val BASE_URL = "https://www.onemap.gov.sg/"

    private val service: Service by lazy {
        val retroFit =
            Retrofit.Builder().addConverterFactory(getJsonConverterFactory()).baseUrl(BASE_URL)
                .build()
        retroFit.create(Service::class.java)
    }

    private val rateBucket = SuspendingBucket.build {
        addLimit(
            Bandwidth.classic(200, Refill.intervally(200, 1.minutes.toJavaDuration()))
                .withInitialTokens(175)
        )
    }

    private var __token: String? = null
    private var __tokenExpiry: Instant = Instant.DISTANT_PAST
    private val __tokenLock = Mutex()

    private val hasTokenExpired: Boolean get() = clock.now() >= __tokenExpiry

    /**
     * Rotates the token every [TOKEN_REFRESH_DURATION]
     */
    suspend fun getToken(): String {
        if (!hasTokenExpired) {
            val tok = __token
            if (tok != null) return tok
        }

        val token: String = __tokenLock.withLock {
            val tok = __token
            when (hasTokenExpired || tok == null) {
                false -> tok
                true -> coroutineScope {
                    rateBucket.consume(1)
                    val result =
                        this@OneMapApiImpl.guarded("onemap.getToken") { service.getToken(CREDENTIALS) }
                    __token = result.accessToken
                    __tokenExpiry = Instant.fromEpochSeconds(result.expiryTimeStamp)
                    result.accessToken
                }
            }
        }

        return token
    }

    override suspend fun lookupPossibleAddresses(epsg3414: String): List<String> {
        rateBucket.consume(1)
        return this@OneMapApiImpl.guarded("onemap.reverseGeocodeEpsg3414") {
            service.reverseGeocodeEpsg3414(
                token = getToken(), location = epsg3414
            )
        }.geoCodeInfo.decode()
    }

    override suspend fun lookupPossibleAddresses(epsg4326: Coordinate): List<String> {
        rateBucket.consume(1)
        return this@OneMapApiImpl.guarded("onemap.reverseGeocodeEpsg4326") {
            service.reverseGeocodeEpsg4326(
                token = getToken(), location = "${epsg4326.latitude},${epsg4326.longitude}"
            )
        }.geoCodeInfo.decode()
    }

    override suspend fun convertEpsg3414To4326(epsg3414: String): Coordinate {
        rateBucket.consume(1)
        val coords = epsg3414.split(',')
        return this@OneMapApiImpl.guarded("onemap.convertEpsg3414To4326") {
            service.convert3414to4326(
                token = getToken(),
                x = coords[0],
                y = coords[1]
            )
        }.let {
            Coordinate.of(it.latitude, it.longitude)
        }
    }

    private interface Service {
        @POST("api/auth/post/getToken")
        suspend fun getToken(@Body credentials: Credentials): RawGetTokenResponse

        @GET("api/public/revgeocodexy")
        suspend fun reverseGeocodeEpsg3414(
            @Header("Authorization") token: String,
            @Query("location") location: String,
            @Query("buffer") buffer: Int = 15,
            @Query("addressType") addressType: String = "All",
            @Query("otherFeatures") otherFeatures: String = "Y"
        ): RawReverseGeocodeEpsg3414Response

        @GET("api/public/revgeocode")
        suspend fun reverseGeocodeEpsg4326(
            @Header("Authorization") token: String,
            @Query("location") location: String,
            @Query("buffer") buffer: Int = 15,
            @Query("addressType") addressType: String = "All",
            @Query("otherFeatures") otherFeatures: String = "Y"
        ): RawReverseGeocodeEpsg3414Response

        @GET("api/common/convert/3414to4326")
        suspend fun convert3414to4326(
            @Header("Authorization") token: String, @Query("X") x: String, @Query("Y") y: String
        ): RawConvert3414To4326Response
    }
}

@Serializable
private data class Credentials(val email: String, val password: String)

private fun RawReverseGeocodeEpsg3414Response.Inner.decode(): String {
    val buildingName = when (this.buildingName) {
        "null" -> ""
        "SINGAPORE - MAIN ISLAND" -> ""
        "" -> ""
        else -> "${this.buildingName},"
    }
    val block = when (this.block) {
        "NIL" -> ""
        else -> this.block
    }
    val road = when (this.road) {
        "NIL" -> ""
        else -> this.road
    }
    val postalCode = when (this.postalCode) {
        "NIL" -> ""
        else -> "SINGAPORE ${this.postalCode}"
    }
    return listOf(buildingName, block, road, postalCode).filter { it.isNotBlank() }
        .joinToString(" ")
}

private fun List<RawReverseGeocodeEpsg3414Response.Inner>.decode() =
    map { it.decode() }.filter { it.isNotBlank() }
