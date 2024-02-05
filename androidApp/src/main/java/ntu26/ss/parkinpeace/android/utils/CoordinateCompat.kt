package ntu26.ss.parkinpeace.android.utils

import android.location.Location
import com.mapbox.geojson.Point
import ntu26.ss.parkinpeace.models.Coordinate
import ntu26.ss.parkinpeace.models.of
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

fun Coordinate.toPoint(): Point = Point.fromLngLat(longitude.toDouble(), latitude.toDouble())
fun Coordinate.Companion.of(point: Point): Coordinate = Coordinate.of(point.latitude(), point.longitude())
fun Coordinate.Companion.of(location: Location): Coordinate = Coordinate.of(location.latitude, location.longitude)
fun Point.toCoordinate(): Coordinate = Coordinate.of(this)
fun Location.toCoordinate(): Coordinate = Coordinate.of(this)

val Coordinate.urlSafe get() = URLEncoder.encode(asIso6709(), StandardCharsets.UTF_8.toString())
fun Coordinate.Companion.urlDecode(string: String?) =
    string?.let { parseOrNull(URLDecoder.decode(string, StandardCharsets.UTF_8.toString())) }