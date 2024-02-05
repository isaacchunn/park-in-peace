package ntu26.ss.parkinpeace.models

interface Location {
    val name: String
    val address: String
    val epsg4326: Coordinate
}