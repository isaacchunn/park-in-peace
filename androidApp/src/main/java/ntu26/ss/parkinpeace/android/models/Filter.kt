package ntu26.ss.parkinpeace.android.models

import ntu26.ss.parkinpeace.models.ParkingSystem
import ntu26.ss.parkinpeace.models.VehicleType

@Deprecated("Use NamedFilter")
interface Filter {
    fun apply(list: List<CarparkAvailability>): List<CarparkAvailability>
}

@Deprecated("Use NamedFilter")
class PriceFilter (
    private var upperBound: Double?,
    private var lowerBound : Double?
): Filter {
    override fun apply(list: List<CarparkAvailability>): List<CarparkAvailability> {

        return list.filter { availability ->
            val carpark = availability.info
            val applicableLots = carpark?.getApplicableLots(VehicleType.CAR)
            val filteredLots = applicableLots?.filter {
                it.rate >= (getLowerBound() ?: Double.MIN_VALUE) || it.rate <= (getUpperBound()
                    ?: Double.MAX_VALUE)
            }
            true
        }
    }


    fun getUpperBound(): Double? {
        return upperBound
    }

    fun setUpperBound(upperBound: Double?) {
        this.upperBound = upperBound
    }

    fun getLowerBound() : Double?{
        return lowerBound
    }

    fun setLowerBound(lowerBound: Double?){
        this.lowerBound = lowerBound
    }
}

@Deprecated("Use NamedFilter")
class DistanceFilter(
    private var upperBound: Double?,
    private var lowerBound: Double?
):Filter {
    override fun apply(list: List<CarparkAvailability>): List<CarparkAvailability> {
        //filters out based on distance, and makes sure it's within lower and upper bound
        val filteredList = list.filter {
            it.distance?.let { distance ->
                distance >= (getLowerBound() ?: Double.MIN_VALUE) && distance <= (getUpperBound()
                    ?: Double.MAX_VALUE)
            } ?: false
        }
        return filteredList
    }

    fun getUpperBound(): Double? {
        return upperBound
    }

    fun setUpperBound(upperBound: Double?) {
        this.upperBound = upperBound
    }

    fun getLowerBound() : Double?{
        return lowerBound
    }

    fun setLowerBound(lowerBound: Double?){
        this.lowerBound = lowerBound
    }
}

@Deprecated("Use NamedFilter")
class PaymentModeFilter(
    private var accept : ParkingSystem
):Filter {
    override fun apply(list: List<CarparkAvailability>): List<CarparkAvailability> {
        //check where to find parking system
        return list.filter {
            it.info?.lots?.getOrNull(0)?.system == getAccept()
        }
    }

    fun getAccept(): ParkingSystem {
        return accept
    }

    fun setAccept(accept: ParkingSystem) {
        this.accept = accept
    }

}


@Deprecated("Use NamedFilter")
class VehicleTypeFilter(
    private var accept: VehicleType
) : Filter {
    override fun apply(list: List<CarparkAvailability>): List<CarparkAvailability> {
        //val car_parkList: List<CarparkAvailability> = listOf()

        //assuming the accept variable refers to the type of vehicle user owns
        return list.filter { it.lots[getAccept()] != null }
    }


    fun getAccept(): VehicleType {
        return accept
    }

    fun setAccept(accept: VehicleType) {
        this.accept = accept
    }
}

@Deprecated("Use NamedFilter")
class MinimumLotsFilter(
    private var type: VehicleType,
    private var minimum : Int
):Filter {
    override fun apply(list: List<CarparkAvailability>): List<CarparkAvailability> {

        //if minimum is the threshold, the displayed carpark lots should be greater than or equal to minimum
        val list_filter = list.filter { it.lots[getType()]!!.current!! >= getMinimum() }
        return list_filter
    }

    fun getType(): VehicleType {
        return type
    }

    fun setType(type: VehicleType) {
        this.type = type
    }

    fun getMinimum() : Int{
        return minimum
    }

    fun setMinimum(minimum: Int){
        this.minimum = minimum
    }
}