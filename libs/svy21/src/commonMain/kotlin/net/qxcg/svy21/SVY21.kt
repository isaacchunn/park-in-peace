package net.qxcg.svy21

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * @author cgcai
 *
 *
 * The SVY21 class provides functionality to convert between the SVY21 and Lat/Lon coordinate systems.
 *
 *
 * Internally, the class uses the equations specified in the following web page to perform the conversion.
 * http://www.linz.govt.nz/geodetic/conversion-coordinates/projection-conversions/transverse-mercator-preliminary-computations/index.aspx
 */
object SVY21 {
    private const val radRatio = PI / 180 // Ratio to convert degrees to radians.

    // Datum and Projection
    private const val a = 6378137 // Semi-major axis of reference ellipsoid.
    private const val f = 1 / 298.257223563 // Ellipsoidal flattening.
    private const val oLat = 1.366666 // Origin latitude (degrees).
    private const val oLon = 103.833333 // Origin longitude (degrees).
    private const val No = 38744.572 // False Northing.
    private const val Eo = 28001.642 // False Easting.
    private const val k = 1 // Central meridian scale factor.

    // Computed Projection Constants
    // Naming convention: the trailing number is the power of the variable.
    private const val b = a * (1 - f) // Semi-minor axis of reference ellipsoid.
    private const val e2 = (2 * f) - (f * f) // Squared eccentricity of reference ellipsoid.
    private const val e4 = e2 * e2
    private const val e6 = e4 * e2
    private const val n = (a - b) / (a + b)
    private const val n2 = n * n
    private const val n3 = n2 * n
    private const val n4 = n2 * n2
    private const val G = a * (1 - n) * (1 - n2) * (1 + (9 * n2 / 4) + (225 * n4 / 64)) * (PI/ 180)

    // Naming convention: A0..6 are terms in an expression, not powers.
    private const val A0 = 1 - (e2 / 4) - (3 * e4 / 64) - (5 * e6 / 256)
    private const val A2 = (3.0 / 8.0) * (e2 + (e4 / 4) + (15 * e6 / 128))
    private const val A4 = (15.0 / 256.0) * (e4 + (3 * e6 / 4))
    private const val A6 = 35 * e6 / 3072

    private fun calcM(lat: Double): Double {                    // M: meridian distance.
        val latR = lat * PI / 180
        return a * ((A0 * latR) - (A2 * sin(2 * latR)) + (A4 * sin(4 * latR)) - (A6 * sin(
            6 * latR
        )))
    }

    private fun calcRho(sin2Lat: Double): Double {                // Rho: radius of curvature of meridian.
        val num = a * (1 - e2)
        val denom = (1 - e2 * sin2Lat).pow(3.0 / 2.0)
        return num / denom
    }

    private fun calcV(sin2Lat: Double): Double {                // v: radius of curvature in the prime vertical.
        val poly = 1 - e2 * sin2Lat
        return a / sqrt(poly)
    }

    /**
     * Computes Latitude and Longitude based on an SVY21 coordinate.
     *
     *
     * This method returns an immutable LatLonCoordiante object that contains two fields,
     * latitude, accessible with .getLatitude(), and
     * longitude, accessible with .getLongitude().
     *
     * @param N Northing based on SVY21.
     * @param E Easting based on SVY21.
     * @return the conversion result a LatLonCoordinate.
     */
    fun computeLatLon(N: Double, E: Double): LatLonCoordinate {
        val Nprime = N - No
        val Mo = calcM(oLat)
        val Mprime = Mo + (Nprime / k)
        val sigma = (Mprime * Math.PI) / (180.0 * G)

        // Naming convention: latPrimeT1..4 are terms in an expression, not powers.
        val latPrimeT1 = ((3 * n / 2) - (27 * n3 / 32)) * sin(2 * sigma)
        val latPrimeT2 = ((21 * n2 / 16) - (55 * n4 / 32)) * sin(4 * sigma)
        val latPrimeT3 = (151 * n3 / 96) * sin(6 * sigma)
        val latPrimeT4 = (1097 * n4 / 512) * sin(8 * sigma)
        val latPrime = sigma + latPrimeT1 + latPrimeT2 + latPrimeT3 + latPrimeT4

        // Naming convention: sin2LatPrime = "square of sin(latPrime)" = Math.pow(sin(latPrime), 2.0)
        val sinLatPrime = sin(latPrime)
        val sin2LatPrime = sinLatPrime * sinLatPrime

        // Naming convention: the trailing number is the power of the variable.
        val rhoPrime = calcRho(sin2LatPrime)
        val vPrime = calcV(sin2LatPrime)
        val psiPrime = vPrime / rhoPrime
        val psiPrime2 = psiPrime * psiPrime
        val psiPrime3 = psiPrime2 * psiPrime
        val psiPrime4 = psiPrime3 * psiPrime
        val tPrime = tan(latPrime)
        val tPrime2 = tPrime * tPrime
        val tPrime4 = tPrime2 * tPrime2
        val tPrime6 = tPrime4 * tPrime2
        val Eprime = E - Eo
        val x = Eprime / (k * vPrime)
        val x2 = x * x
        val x3 = x2 * x
        val x5 = x3 * x2
        val x7 = x5 * x2

        // Compute Latitude
        // Naming convention: latTerm1..4 are terms in an expression, not powers.
        val latFactor = tPrime / (k * rhoPrime)
        val latTerm1 = latFactor * ((Eprime * x) / 2)
        val latTerm2 =
            latFactor * ((Eprime * x3) / 24) * ((-4 * psiPrime2) + (9 * psiPrime) * (1 - tPrime2) + (12 * tPrime2))
        val latTerm3 =
            latFactor * ((Eprime * x5) / 720) * ((8 * psiPrime4) * (11 - 24 * tPrime2) - (12 * psiPrime3) * (21 - 71 * tPrime2) + (15 * psiPrime2) * (15 - 98 * tPrime2 + 15 * tPrime4) + (180 * psiPrime) * (5 * tPrime2 - 3 * tPrime4) + 360 * tPrime4)
        val latTerm4 =
            latFactor * ((Eprime * x7) / 40320) * (1385 - 3633 * tPrime2 + 4095 * tPrime4 + 1575 * tPrime6)
        val lat = latPrime - latTerm1 + latTerm2 - latTerm3 + latTerm4

        // Compute Longitude
        // Naming convention: lonTerm1..4 are terms in an expression, not powers.
        val secLatPrime = 1.0 / cos(lat)
        val lonTerm1 = x * secLatPrime
        val lonTerm2 = ((x3 * secLatPrime) / 6) * (psiPrime + 2 * tPrime2)
        val lonTerm3 =
            ((x5 * secLatPrime) / 120) * ((-4 * psiPrime3) * (1 - 6 * tPrime2) + psiPrime2 * (9 - 68 * tPrime2) + 72 * psiPrime * tPrime2 + 24 * tPrime4)
        val lonTerm4 =
            ((x7 * secLatPrime) / 5040) * (61 + 662 * tPrime2 + 1320 * tPrime4 + 720 * tPrime6)
        val lon = (oLon * PI / 180) + lonTerm1 - lonTerm2 + lonTerm3 - lonTerm4
        return LatLonCoordinate(lat / (PI / 180), lon / (PI / 180))
    }

    /**
     * Computes Latitude and Longitude based on an SVY21 coordinate.
     *
     *
     * This method returns an immutable LatLonCoordiante object that contains two fields,
     * latitude, accessible with .getLatitude(), and
     * longitude, accessible with .getLongitude().
     *
     *
     * This method is a shorthand for the functionally identical
     * public LatLonCoordinate computeLatLon(double N, double E).
     *
     * @param coord an SVY21Coordinate object to convert.
     * @return the conversion result a LatLonCoordinate.
     */
    @JvmStatic
    fun computeLatLon(coord: SVY21Coordinate): LatLonCoordinate {
        val northing = coord.northing
        val easting = coord.easting
        return computeLatLon(northing, easting)
    }

    /**
     * Computes SVY21 Northing and Easting based on a Latitude and Longitude coordinate.
     *
     *
     * This method returns an immutable SVY21Coordinate object that contains two fields,
     * northing, accessible with .getNorthing(), and
     * easting, accessible with .getEasting().
     *
     * @param lat latitude in degrees.
     * @param lon longitude in degrees.
     * @return the conversion result as an SVY21Coordinate.
     */
    fun computeSVY21(lat: Double, lon: Double): SVY21Coordinate {
        // Naming convention: sin2Lat = "square of sin(lat)" = Math.pow(sin(lat), 2.0)
        val latR = lat * radRatio
        val sinLat = Math.sin(latR)
        val sin2Lat = sinLat * sinLat
        val cosLat = Math.cos(latR)
        val cos2Lat = cosLat * cosLat
        val cos3Lat = cos2Lat * cosLat
        val cos4Lat = cos3Lat * cosLat
        val cos5Lat = cos3Lat * cos2Lat
        val cos6Lat = cos5Lat * cosLat
        val cos7Lat = cos5Lat * cos2Lat
        val rho = calcRho(sin2Lat)
        val v = calcV(sin2Lat)
        val psi = v / rho
        val t = Math.tan(latR)
        val w = (lon - oLon) * radRatio
        val M = calcM(lat)
        val Mo = calcM(oLat)

        // Naming convention: the trailing number is the power of the variable.
        val w2 = w * w
        val w4 = w2 * w2
        val w6 = w4 * w2
        val w8 = w6 * w2
        val psi2 = psi * psi
        val psi3 = psi2 * psi
        val psi4 = psi2 * psi2
        val t2 = t * t
        val t4 = t2 * t2
        val t6 = t4 * t2

        // Compute Northing.
        // Naming convention: nTerm1..4 are terms in an expression, not powers.
        val nTerm1 = w2 / 2 * v * sinLat * cosLat
        val nTerm2 = w4 / 24 * v * sinLat * cos3Lat * (4 * psi2 + psi - t2)
        val nTerm3 =
            w6 / 720 * v * sinLat * cos5Lat * (8 * psi4 * (11 - 24 * t2) - 28 * psi3 * (1 - 6 * t2) + psi2 * (1 - 32 * t2) - psi * 2 * t2 + t4)
        val nTerm4 = w8 / 40320 * v * sinLat * cos7Lat * (1385 - 3111 * t2 + 543 * t4 - t6)
        val N = No + k * (M - Mo + nTerm1 + nTerm2 + nTerm3 + nTerm4)

        // Compute Easting.
        // Naming convention: eTerm1..3 are terms in an expression, not powers.
        val eTerm1 = w2 / 6 * cos2Lat * (psi - t2)
        val eTerm2 =
            w4 / 120 * cos4Lat * (4 * psi3 * (1 - 6 * t2) + psi2 * (1 + 8 * t2) - psi * 2 * t2 + t4)
        val eTerm3 = w6 / 5040 * cos6Lat * (61 - 479 * t2 + 179 * t4 - t6)
        val E = Eo + k * v * w * cosLat * (1 + eTerm1 + eTerm2 + eTerm3)
        return SVY21Coordinate(N, E)
    }

    /**
     * Computes SVY21 Northing and Easting based on a Latitude and Longitude coordinate.
     *
     *
     * This method returns an immutable SVY21Coordinate object that contains two fields,
     * northing, accessible with .getNorthing(), and
     * easting, accessible with .getEasting().
     *
     *
     * This method is a shorthand for the functionally identical
     * public SVY21Coordinate computeSVY21(double lat, double lon).
     *
     * @param coord a LatLonCoordinate object to convert.
     * @return the conversion result an SVY21Coordinate.
     */
    fun computeSVY21(coord: LatLonCoordinate): SVY21Coordinate {
        val latitude = coord.latitude
        val longitude = coord.longitude
        return computeSVY21(latitude, longitude)
    }
}