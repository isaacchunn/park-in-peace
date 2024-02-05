package ntu26.ss.parkinpeace.android.services

import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import net.qxcg.svy21.LatLonCoordinate
import net.qxcg.svy21.SVY21Coordinate
import ntu26.ss.parkinpeace.IOFlow
import ntu26.ss.parkinpeace.android.api.ura.UraApi
import ntu26.ss.parkinpeace.android.api.ura.UraApiImpl
import ntu26.ss.parkinpeace.android.models.Carpark
import ntu26.ss.parkinpeace.android.models.CarparkAvailability
import ntu26.ss.parkinpeace.models.*
import java.time.Instant
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import kotlin.random.Random

@Deprecated("Use NavigationWithPipManager instead")
class NavigationManager() : NavigationService {
    var isNavigating: Boolean = false;
    var navigatingTo: Location? = null;
    var navigatingToCarpark: CarparkAvailability? = null
    var standByCarparks: List<CarparkAvailability> = listOf()
    var selectedCarpark: CarparkAvailability? = null

    // Make an instance of the api to be used for subsequent calls
    val api: UraApi = UraApiImpl()

    override val carparkID: String
        get() = TODO("Not yet implemented")

    /**
     *
     */
    override val authenticated: Boolean
        get() = api.authenticated

    override suspend fun subscribe(carparkId: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun unsubscribe() {
        TODO("Not yet implemented")
    }

    override suspend fun queryNearby(
        origin: Coordinate,
        destination: Coordinate,
        options: NavigationService.QueryOptions
    ): IOFlow<CarparkAvailability> {
        //Store a map for temporary storing + some list
        val carparkMap: MutableMap<String, CarparkAvailability> = mutableMapOf()

        //Get each individual carparks for now and their availability
        val carparks = api.getCarparkListAndRates()
        val carparkLots = api.getCarparkLots()
        if (carparks.result == null || carparkLots.result == null) {
            return IOFlow(0, emptyFlow())
        }
        //Conduct a filter on carparks that have price for now until we find an alternative
        val filteredCarparks =
            carparks.result!!.filter { it != null && it.parkingSystem == "B" && it.geometries!!.size != 0 }
        filteredCarparks?.forEach { carpark ->
            if (carpark != null) {
                /*
                  val parkingSystem = when (carpark.parkingSystem) {
                      "C" -> ParkingSystem.COUPON
                      "B" -> ParkingSystem.ELECTRONIC
                      else -> throw IllegalStateException("Failed to parse \"${carpark.parkingSystem}\" as parkingsystem")
                  }
                   */

                //Parse our lat lon using format
                val latlon = UraI18n.parseCoordinateOrDefault(carpark.geometries!!.get(0)!!.coordinates.toString())
                val parkingSystem = ParkingSystem.ELECTRONIC
                //Check if our current map has this, else we make a new carpark
                if (carparkMap[carpark.ppCode] == null) {
                    val newCarpark = Carpark(
                        id = "0000000000000000000",
                        ref = carpark.ppCode?.let { "ura/$it" } ?: "ura/__001",
                        name = carpark.ppName ?: "Placeholder Name",
                        address = "gg idk no location translation",
                        epsg4326 = Coordinate.of(latlon.latitude, latlon.longitude),
                        lots = listOf(
                            Lot(
                                vehicleType = VehicleType.CAR,
                                chargeType = ChargeType.WEEKDAY,
                                startTime = UraI18n.parseTimeOrDefault(
                                    carpark.startTime,
                                    LocalTime.NOON
                                ),
                                endTime = UraI18n.parseTimeOrDefault(
                                    carpark.endTime,
                                    LocalTime.MIDNIGHT
                                ),
                                rate = UraI18n.parseRateOrDefault(
                                    carpark.weekdayRate,
                                    50
                                ), // 50 cents
                                minDuration = UraI18n.parseMinTimeOrDefault(carpark.weekdayMin), // 30 mins
                                capacity = carpark.parkCapacity ?: -1,
                                system = parkingSystem
                            ),
                            Lot(
                                vehicleType = VehicleType.CAR,
                                chargeType = ChargeType.SATURDAY,
                                startTime = UraI18n.parseTimeOrDefault(
                                    carpark.startTime,
                                    LocalTime.NOON
                                ),
                                endTime = UraI18n.parseTimeOrDefault(
                                    carpark.endTime,
                                    LocalTime.MIDNIGHT
                                ),
                                rate = UraI18n.parseRateOrDefault(
                                    carpark.satdayRate,
                                    50
                                ), // 50 cents
                                minDuration = UraI18n.parseMinTimeOrDefault(carpark.satdayMin), // 30 mins
                                capacity = carpark.parkCapacity ?: -1,
                                system = parkingSystem
                            ),
                            Lot(
                                vehicleType = VehicleType.CAR,
                                chargeType = ChargeType.SUNDAY_PH,
                                startTime = UraI18n.parseTimeOrDefault(
                                    carpark.startTime,
                                    LocalTime.NOON
                                ),
                                endTime = UraI18n.parseTimeOrDefault(
                                    carpark.endTime,
                                    LocalTime.MIDNIGHT
                                ),
                                rate = UraI18n.parseRateOrDefault(
                                    carpark.sunPHRate,
                                    50
                                ), // 50 cents
                                minDuration = UraI18n.parseMinTimeOrDefault(carpark.sunPHMin), // 30 mins
                                capacity = carpark.parkCapacity ?: -1,
                                system = parkingSystem
                            )
                        ),
                        features = listOf(Feature.VEHICLE_WASHING, Feature.ELECTRIC_CHARGING),
                        hash = "e008ba221c8dad980ec850805b37de72fba9ef6c022a195f554792aa8b08b244"
                    )
                    //Add into our map
                    carparkMap[newCarpark.ref] = CarparkAvailability(
                        id = newCarpark.id,
                        info = newCarpark,
                        origin = newCarpark.epsg4326,
                        distance = Random.nextInt(0, 50),
                        travelTime = 0,
                        lots = mapOf(
                            VehicleType.CAR to CarparkAvailability.Inner(0, 0),
                            VehicleType.MOTORCYCLE to CarparkAvailability.Inner(0, 0),
                            VehicleType.HEAVY_VEHICLE to CarparkAvailability.Inner(0, 0)
                        ),
                        asof = Instant.now()
                    )
                }
            }
        }
        //Loop through the lots that we have gotten
        carparkLots.result.forEach { carpark ->
            if (carpark != null) {
                val lotType = when (carpark.lotType) {
                    "C" -> VehicleType.CAR
                    "M" -> VehicleType.MOTORCYCLE
                    "H" -> VehicleType.HEAVY_VEHICLE
                    else -> throw IllegalStateException("Failed to parse \"${carpark.lotType}\" as a lot type.")
                }
                val currentLots = carpark.lotsAvailable
                //Then there exists such an object already, then
                if (carparkMap[carpark.carparkNo] != null) {
                    //Instantiate a new carpark availability app since we are swapping the values
                    val availability = carparkMap[carpark.carparkNo]
                    var lotMap = availability!!.lots.toMutableMap()
                    lotMap[lotType] = CarparkAvailability.Inner(carpark.lotsAvailable!!.toInt(), 0)
                    carparkMap.put(
                        carpark.carparkNo!!, CarparkAvailability(
                            id = availability.info!!.id,
                            info = availability.info,
                            origin = availability.origin,
                            distance = availability.distance,
                            travelTime = availability.travelTime,
                            lots = lotMap.toMap(),
                            asof = availability.asof
                        )
                    )
                }
            }
        }
        standByCarparks = carparkMap.values.toList()
        return IOFlow(standByCarparks.size, standByCarparks.asFlow())
    }

    override suspend fun warnCarparkFull(carparkId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun refreshCarparkInfo(carparkId: String): CarparkAvailability? {
        TODO("Not yet implemented")
    }
}


/**
 * Class with knowledge of URA-specific formats
 */
private object UraI18n {
    private val uraTimeFormatter: DateTimeFormatter = DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .appendPattern("hh.mm a")
        .toFormatter()

    /**
     * Parse URA specific time format to [java.time.LocalTime] object
     */
    @JvmStatic
    fun parseTimeOrDefault(string: String?, default: LocalTime): LocalTime {
        if (string == null) return default
        return try {
            LocalTime.parse(string, uraTimeFormatter)
        } catch (e: DateTimeParseException) {
            default
        }
    }

    /**
     * Parse URA $ format to Int
     */
    fun parseRateOrDefault(string: String?, default: Int): Int {
        if (string == null) return default
        val rate = string.drop(1).toDouble() * 100
        return rate.toInt()
    }


    /**
     * Parse URA minTime format to Int in minutes, where 30 is the default minimum.
     */
    fun parseMinTimeOrDefault(string: String?, default: Int = 30): Int {
        if (string == null) return default
        val minTime = string.filter { it.isDigit() }
        return minTime.toInt()
    }

    fun parseCoordinateOrDefault(
        string: String?,
        default: LatLonCoordinate = LatLonCoordinate(1.23456, 6.7890)
    ): LatLonCoordinate {
        if (string == null) return default
        //Parse our string
        //Assume the format is as such 28902.6905,28558.6655
        val parsedCoords = string.split(",")
        val northing = parsedCoords[1].toDouble()
        val easting = parsedCoords[0].toDouble()
        //Split the
        val coord = SVY21Coordinate(northing,easting)
        return coord.asLatLon()
    }
}