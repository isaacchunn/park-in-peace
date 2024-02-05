package ntu26.ss.parkinpeace.android.viewmodels

import kotlinx.coroutines.flow.*
import ntu26.ss.parkinpeace.android.models.*
import ntu26.ss.parkinpeace.models.Coordinate
import ntu26.ss.parkinpeace.models.Location
import ntu26.ss.parkinpeace.models.VehicleType

/**
 * Inject into viewmodels to get a single source of truth for vehicleType, searchRadius, customLocations.
 *
 * STICKY.
 */
interface PreferencesMixin {
    val vehicle: StateFlow<VehicleType>
    val searchRadius: StateFlow<Int>
    val customLocations: StateFlow<List<StoredLocation>>

    fun setVehicle(value: VehicleType)
    fun setSearchRadius(value: Int)
    fun setCustomLocations(value: List<StoredLocation>)
}

/**
 * Injected into viewmodels to get a single source of truth for nearbyCarparks, finalCarparks, sortingStrategy, filters.
 *
 * NON-STICKY.
 */
interface NearbyCarparkMixin {
    val sortingStrategy: StateFlow<SortingStrategy>
    val filters: Flow<NamedFilters>
    val nearbyCarparks: StateFlow<List<CarparkAvailability>>
    val finalCarparks: Flow<List<CarparkAvailability>>

    fun setSortingStrategy(value: SortingStrategy)
    fun setFilters(value: NamedFilters)
    fun modifyFilters(block: (NamedFilters) -> NamedFilters)
    fun setNearbyCarparks(value: List<CarparkAvailability>)
}

interface HistoryMixin {
    val history: StateFlow<List<Location>>

    fun setHistory(value: List<Location>)
    fun modifyHistory(block: (List<Location>) -> List<Location>)
}

interface MapMixin : PreferencesMixin, NearbyCarparkMixin

private val PRESET_CUSTOM_LOCATIONS = listOf(
    StoredLocation(
        "Home",
        "BLK 408 FERNVALE ROAD",
        "CORAL VALE, 408 FERNVALE ROAD SINGAPORE 790408",
        Coordinate.parse("+01.3888828+103.8767195/")
    ),
    StoredLocation(
        "Work",
        "BACKPACKERS@SG",
        "111J KING GEORGE'S AVENUE, BACKPACKERS@SG SINGAPORE 208559",
        Coordinate.parse("+01.3103816+103.8617578/")
    ),
    StoredLocation(
        "Orchard",
        "ION Orchard",
        "ION ORCHARD, 2 ORCHARD TURN SINGAPORE 238801",
        Coordinate.parse("+01.30403+103.83206/")
    ),
    StoredLocation(
        "Mom's",
        "SEMBAWANG HILLS FOOD CENTRE OFF STREET",
        "SEMBAWANG HILLS FOOD CENTRE, 590 UPPER THOMSON ROAD SINGAPORE 574419",
        Coordinate.parse("+01.3722655+103.8288232/")
    )
)

private val PRESET_SORTING_STRATEGY =
    SortingStrategy(SortingStrategy.Mode.AVAILABILITY_CURRENT, SortingStrategy.Order.DESCENDING)

val PRESET_FILTERS = NamedFilters(
    agencyFilter = NamedFilter.AgencyFilter(
        mapOf(
            Agency.URA to true,
            Agency.LTA to true,
            Agency.HDB to true
        )
    ),
    vacancyFilter = NamedFilter.VacancyFilter(null, vehicleType = VehicleType.CAR)
)

/**
 * In-memory store. Data lost when app closes. For demo only.
 */
internal val MemoryDemoMixin = object : MapMixin {
    private val _vehicle = MutableStateFlow(VehicleType.CAR)
    private val _searchRadius = MutableStateFlow(500)
    private val _customLocations = MutableStateFlow(PRESET_CUSTOM_LOCATIONS)
    private val _sortingStrategy = MutableStateFlow(PRESET_SORTING_STRATEGY)
    private val _filters = MutableStateFlow(PRESET_FILTERS)
    private val _nearbyCarparks = MutableStateFlow(listOf<CarparkAvailability>())

    private val _vfilters = _filters.combine(_vehicle) { filt, veh ->
        filt.updatedVehicle(veh) to veh
    }

    private val _svfilters = _sortingStrategy.combine(_vfilters) { sort, (filt, veh) ->
        Triple(sort, filt, veh)
    }

    private val _finalCarparks = _nearbyCarparks.combine(_svfilters) { lst, (sort, filt, veh) ->
        val isHoliday = false
        filt.apply(lst).apply(sort, veh, isHoliday)
    }

    override val vehicle: StateFlow<VehicleType> = _vehicle
    override val searchRadius: StateFlow<Int> = _searchRadius
    override val customLocations: StateFlow<List<StoredLocation>> = _customLocations
    override val sortingStrategy: StateFlow<SortingStrategy> = _sortingStrategy
    override val filters: Flow<NamedFilters> = _vfilters.map { it.first }
    override val nearbyCarparks: StateFlow<List<CarparkAvailability>> = _nearbyCarparks
    override val finalCarparks: Flow<List<CarparkAvailability>> = _finalCarparks

    override fun setVehicle(value: VehicleType) {
        _vehicle.value = value
    }

    override fun setSearchRadius(value: Int) {
        _searchRadius.value = value
    }

    override fun setCustomLocations(value: List<StoredLocation>) {
        _customLocations.value = value
    }

    override fun setSortingStrategy(value: SortingStrategy) {
        _sortingStrategy.value = value
    }

    override fun setFilters(value: NamedFilters) {
        _filters.value = value
    }

    override fun modifyFilters(block: (NamedFilters) -> NamedFilters) {
        val v = _filters.value
        _filters.value = block(v)
    }

    override fun setNearbyCarparks(value: List<CarparkAvailability>) {
        _nearbyCarparks.value = value
    }
}

/**
 * DB-backed preferences.
 */
internal val DBPreferencesMixin = object : PreferencesMixin {
    override val vehicle: StateFlow<VehicleType>
        get() = TODO("Not yet implemented")
    override val searchRadius: StateFlow<Int>
        get() = TODO("Not yet implemented")
    override val customLocations: StateFlow<List<StoredLocation>>
        get() = TODO("Not yet implemented")

    override fun setVehicle(value: VehicleType) {
        TODO("Not yet implemented")
    }

    override fun setSearchRadius(value: Int) {
        TODO("Not yet implemented")
    }

    override fun setCustomLocations(value: List<StoredLocation>) {
        TODO("Not yet implemented")
    }

}

internal val MemoryDemoHistoryMixin = object : HistoryMixin {
    private val _history = MutableStateFlow(listOf<Location>())
    override val history: StateFlow<List<Location>> = _history

    override fun setHistory(value: List<Location>) {
        _history.value = value
    }

    override fun modifyHistory(block: (List<Location>) -> List<Location>) {
        val c = _history.value
        _history.value = block(c)
    }
}