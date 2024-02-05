package net.qxcg.svy21

import net.qxcg.svy21.SVY21.computeLatLon

data class SVY21Coordinate(val northing: Double, val easting: Double) {
    fun asLatLon(): LatLonCoordinate {
        return computeLatLon(this)
    }
}