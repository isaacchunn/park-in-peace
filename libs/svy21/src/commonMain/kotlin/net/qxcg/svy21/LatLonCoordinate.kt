package net.qxcg.svy21

data class LatLonCoordinate(val latitude: Double, val longitude: Double) {
    fun asSVY21(): SVY21Coordinate {
        return SVY21.computeSVY21(this)
    }
}