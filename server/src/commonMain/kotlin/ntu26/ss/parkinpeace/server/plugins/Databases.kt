package ntu26.ss.parkinpeace.server.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.datetime.Clock
import ntu26.ss.parkinpeace.models.ChargeType
import ntu26.ss.parkinpeace.models.Coordinate
import ntu26.ss.parkinpeace.models.Feature
import ntu26.ss.parkinpeace.models.Lot
import ntu26.ss.parkinpeace.models.ParkingSystem
import ntu26.ss.parkinpeace.models.VehicleType
import ntu26.ss.parkinpeace.server.data.RawDbCarpark
import ntu26.ss.parkinpeace.server.data.db.AvailabilityDao
import ntu26.ss.parkinpeace.server.data.db.RawCarparkDao
import ntu26.ss.parkinpeace.server.models.Availability
import ntu26.ss.parkinpeace.server.models.Carpark
import org.jetbrains.exposed.sql.Database
import org.koin.ktor.ext.inject
import ulid.ULID
import java.time.LocalTime

fun Application.configureDatabases() {
    val database: Database by inject()
    val rawCarparkDao: RawCarparkDao by inject()
    val availabilityDao: AvailabilityDao by inject()
    var count = 0

    routing {
        get("/testcp0") {
            count++
            RawDbCarpark.Factory().create(
                ref = "ura/__%03d".format(count),
                name = "Dummy Carpark $count",
                address = "NTU",
                epsg4326 = Coordinate.parse("+01.343059+103.6809727/"),
                lots = listOf(
                    Lot(
                        vehicleType = VehicleType.CAR,
                        chargeType = ChargeType.WEEKDAY,
                        startTime = LocalTime.of(8, 30), // 8:30 AM
                        endTime = LocalTime.of(17, 0), // 5:00 PM
                        rate = 50, // 50 cents
                        minDuration = 30, // 30 mins
                        capacity = 69, // 69 lots
                        system = ParkingSystem.COUPON
                    ),
                    Lot(
                        vehicleType = VehicleType.CAR,
                        chargeType = ChargeType.SATURDAY,
                        startTime = LocalTime.of(8, 30), // 8:30 AM
                        endTime = LocalTime.of(17, 0), // 5:00 PM
                        rate = 50, // 50 cents
                        minDuration = 30, // 30 mins
                        capacity = 69, // 69 lots
                        system = ParkingSystem.COUPON
                    ),
                    Lot(
                        vehicleType = VehicleType.CAR,
                        chargeType = ChargeType.SUNDAY_PH,
                        startTime = LocalTime.of(8, 30), // 8:30 AM
                        endTime = LocalTime.of(17, 0), // 5:00 PM
                        rate = 50, // 50 cents
                        minDuration = 30, // 30 mins
                        capacity = 69, // 69 lots
                        system = ParkingSystem.COUPON
                    )
                ),
                features = listOf(Feature.VEHICLE_WASHING, Feature.ELECTRIC_CHARGING),
                epsg3414 = "13918.6664492646,35767.396894427",
                isActive = true
            ).also {
                rawCarparkDao.create(it)
                call.respond(it)
            }
        }

        get("/testcp1/{id}") {
            val id = call.parameters["id"]!!
            VehicleType.entries.map {
                Availability(
                    carparkId = ULID.parseULID(id),
                    vehicleType = it,
                    asof = Clock.System.now(),
                    availability = (25..642).random()
                )
            }.also {
                availabilityDao.create(it)
                call.respond(it)
            }
        }
    }
}

private fun RawDbCarpark.toCarpark(): Carpark {
    require(isActive) { "Attempted to convert inactive carpark" }
    return Carpark(
        id = id,
        ref = ref,
        name = name,
        address = address ?: throw IllegalArgumentException("address must not be null"),
        epsg4326 = epsg4326 ?: throw IllegalArgumentException("epsg4326 must not be null"),
        lots = lots,
        features = features,
        hash = hash
    )
}