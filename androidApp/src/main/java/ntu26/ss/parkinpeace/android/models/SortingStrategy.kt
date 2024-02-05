package ntu26.ss.parkinpeace.android.models

import ntu26.ss.parkinpeace.models.VehicleType

data class SortingStrategy(val mode: Mode, val order: Order) {
    enum class Mode(val display: String) {
        DISTANCE("Distance"), PRICE("Price"), AVAILABILITY_CURRENT("Vacancy"), TRAVEL_TIME(
            "Travel Time"
        )
    }
    enum class Order(val display: String) { ASCENDING("Ascending"), DESCENDING("Descending") }

    fun cycleMode(): SortingStrategy = copy(mode = mode.cycle())
    fun cycleOrder(): SortingStrategy = copy(order = order.cycle())
}

private inline fun <reified E : Enum<E>> E.cycle(): E {
    val t = enumValues<E>()
    return t[(ordinal + 1) % t.size]
}

fun List<CarparkAvailability>.apply(
    sortingStrategy: SortingStrategy,
    vehicleType: VehicleType,
    isHoliday: Boolean
): List<CarparkAvailability> {
    val sort = when (sortingStrategy.mode) {
        SortingStrategy.Mode.TRAVEL_TIME -> CarparkAvailability::travelTime
        SortingStrategy.Mode.DISTANCE -> CarparkAvailability::distance
        SortingStrategy.Mode.PRICE -> { it: CarparkAvailability ->
            it.info?.getApplicableLots(vehicleType, isHoliday = isHoliday)
                ?.minPriceOrNull()?.rate
        }

        SortingStrategy.Mode.AVAILABILITY_CURRENT -> { it: CarparkAvailability -> it.lots[vehicleType]?.current }
    }
    return when (sortingStrategy.order) {
        SortingStrategy.Order.ASCENDING -> sortedBy(sort)
        SortingStrategy.Order.DESCENDING -> sortedByDescending(sort)
    }
}
