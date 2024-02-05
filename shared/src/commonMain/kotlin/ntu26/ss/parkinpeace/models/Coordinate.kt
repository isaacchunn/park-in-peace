package ntu26.ss.parkinpeace.models

import kotlinx.serialization.Serializable
import ntu26.ss.parkinpeace.models.Coordinate.Companion.MAXIMUM_PREC
import ntu26.ss.parkinpeace.serializers.CoordinateSerializer
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

@Serializable(with = CoordinateSerializer::class)
data class Coordinate private constructor(
    val latitude: BigDecimal,
    val longitude: BigDecimal,
) {
    private val iso6709: String
        get() = String.format(
            "%s%s/", LAT_FORMATTER.format(latitude), LON_FORMATTER.format(longitude)
        )

    fun asIso6709(): String = iso6709

    companion object {
        val MAXIMUM_PREC = 7
        private val LAT_FORMATTER =
            DecimalFormat("+00.${"#".repeat(MAXIMUM_PREC)};-00.${"#".repeat(MAXIMUM_PREC)}")
        private val LON_FORMATTER =
            DecimalFormat("+000.${"#".repeat(MAXIMUM_PREC)};-000.${"#".repeat(MAXIMUM_PREC)}")
        private val REGEX_SPEC = """([\+\-]\d{2,3}\.\d+)""".toRegex()

        private fun MatchResult?.toBigDecimalOrNull(): BigDecimal? =
            this?.destructured?.component1()?.toBigDecimalOrNull()

        fun parseOrNull(iso6709: String?): Coordinate? {
            iso6709 ?: return null
            return REGEX_SPEC.find(iso6709)?.let {
                val lat = it.toBigDecimalOrNull() ?: return null
                it.next()?.let {
                    val lon = it.toBigDecimalOrNull() ?: return null
                    of(lat, lon)
                }
            }
        }

        fun parse(iso6709: String): Coordinate = parseOrNull(iso6709)
            ?: throw IllegalArgumentException("Unable to decode \"$iso6709\" as coordinates")

        /**
         * Converts the coordinates into Coordinate.
         * Do note that this is a lossy conversion. The maximum precision retained is specified by [MAXIMUM_PREC].
         */
        fun of(lat: BigDecimal, lon: BigDecimal): Coordinate =
            Coordinate(
                lat.setScale(MAXIMUM_PREC, RoundingMode.HALF_DOWN),
                lon.setScale(MAXIMUM_PREC, RoundingMode.HALF_DOWN)
            )
    }
}

/**
 * Converts the coordinates into Coordinate.
 * Do note that this is a lossy conversion. The maximum precision retained is specified by [MAXIMUM_PREC].
 */
fun Coordinate.Companion.of(lat: Double, lon: Double): Coordinate =
    of(lat.toBigDecimal(), lon.toBigDecimal())

/**
 * Converts the coordinates into Coordinate.
 * Do note that this is a lossy conversion. The maximum precision retained is specified by [MAXIMUM_PREC].
 */
fun Coordinate.Companion.of(lat: String, lon: String): Coordinate =
    of(lat.toBigDecimal(), lon.toBigDecimal())