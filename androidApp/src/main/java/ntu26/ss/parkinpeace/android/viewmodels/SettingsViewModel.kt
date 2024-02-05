package ntu26.ss.parkinpeace.android.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ntu26.ss.parkinpeace.android.data.UserLocationsDB
import ntu26.ss.parkinpeace.android.data.UserLocationsDao
import ntu26.ss.parkinpeace.android.models.StoredLocation
import ntu26.ss.parkinpeace.models.Coordinate
import ntu26.ss.parkinpeace.models.Location
import ntu26.ss.parkinpeace.models.VehicleType

class SettingsViewModel(application: Application, private val prefsMixin: PreferencesMixin = MemoryDemoMixin) :
    AndroidViewModel(application), PreferencesMixin by prefsMixin {
    constructor(application: Application) : this(application, MemoryDemoMixin)

    var isDialogShown by mutableStateOf(false)
        private set

    var isOtherShown by mutableStateOf(false)
        private set

    //db for recent searchs
    val user_dao: UserLocationsDao =
        UserLocationsDB.buildDatabase(application).dao


    private val _homeData = MutableStateFlow(SavedItem())
    val homeData: StateFlow<SavedItem> = _homeData

    private val _workData = MutableStateFlow(SavedItem())
    val workData: StateFlow<SavedItem> = _workData

    data class SavedItem(
        var name: String = "",
        var address: String = "",
        var location: StoredLocation? = null
    )

    data class OtherItem(
        var id: Int,
        var name: String = "",
        var address: String = "",
        var location: StoredLocation? = null
    ) {}

    private val _otherData = MutableStateFlow<List<OtherItem>>(listOf())
    val otherData: StateFlow<List<OtherItem>> = _otherData

    private var index = 0

    private val _sliderPosition = MutableStateFlow(prefsMixin.searchRadius.value.toFloat())
    val sliderPosition: StateFlow<Float> = _sliderPosition

    private val _sliderLowerBound = MutableStateFlow(100f)
    val sliderLowerBound: StateFlow<Float> = _sliderLowerBound

    private val _sliderUpperBound = MutableStateFlow(500f)
    val sliderUpperBound: StateFlow<Float> = _sliderUpperBound

    private val _selectedOptionText = MutableStateFlow(prefsMixin.vehicle.value.asOptionText())
    val selectedOptionText: StateFlow<String> = _selectedOptionText

    private val _shownOther = MutableStateFlow(0)
    val shownOther: StateFlow<Int> = _shownOther

    private val _selectedLocation = MutableStateFlow(false)
    val selectedLocation: StateFlow<Boolean> = _selectedLocation

    var init = false

    fun setSelectedLocation(state: Boolean) {
        _selectedLocation.value = state
    }

    fun onLocClick() {
        isDialogShown = true
    }

    fun onOtherClick() {
        isOtherShown = true
    }

    fun setHomeLocation(location: Location) {
        val loc = StoredLocation(
            tag = "Home",
            name = location.name,
            address = location.address,
            epsg4326 = location.epsg4326
        )
        homeData.value.location = loc
    }

    fun setWorkLocation(location: Location) {
        val loc = StoredLocation(
            tag = "Work",
            name = location.name,
            address = location.address,
            epsg4326 = location.epsg4326
        )
        workData.value.location = loc
    }

    fun setOtherLocation(idx: Int, location: Location) {
        val loc = StoredLocation(
            tag = "Other",
            name = location.name,
            address = location.address,
            epsg4326 = location.epsg4326
        )
        otherData.value.getOrNull(idx)?.location = loc ?: return
    }

    fun setupInitial() {
        if(init)
            return
        init = true
        //Very crude way to do it
        val godList = prefsMixin.customLocations.value
        if(godList.isNotEmpty())
        {
            val godHomeData = godList[0]
            val godWorkData = godList[1]

            //Update home values
            _homeData.value.name = "Home"
            _homeData.value.address = godHomeData.address
            _homeData.value.location = godHomeData

            //Update work values
            _workData.value.name = "Work"
            _workData.value.address = godWorkData.address
            _workData.value.location = godWorkData

            for (i in 2..<godList.size)
            {
                addLocation("","")
                val godData = godList[i]
                val other = _otherData.value[i-2]
                other.name = godData.tag
                other.address = godData.address
                other.id = i-2
                other.location = godData

            }
        }
    }

    fun editHomeName(name: String) {
        if (name != "") {
            _homeData.value.name = name
        } else if (_homeData.value.name == "Click to add custom location") {
            _homeData.value.name = "NIL"
        }
    }

    fun editHomeAddress(address: String) {
        if (address != "") {
            _homeData.value.address = address
        } else {
            _homeData.value.address = "NIL"
        }
    }

    fun editWorkName(name: String) {
        if (name != "") {
            _workData.value.name = name
        } else if (_workData.value.name == "Click to add custom location") {
            _workData.value.name = "NIL"
        }
    }

    fun editWorkAddress(address: String) {
        if (address != "") {
            _workData.value.address = address
        } else {
            _workData.value.address = "NIL"
        }
    }

    fun editOtherName(id: Int, typedname: String) {
        if (typedname != "") {
            _otherData.value =
                _otherData.value.toMutableList().also { it[id] = it[id].copy(name = typedname) }
                    .toList()
        } else if (getOtherName(id) == "Click to add location") {
            _otherData.value =
                _otherData.value.toMutableList().also { it[id] = it[id].copy(name = "NIL") }
                    .toList()
        }
    }

    fun editOtherAddress(id: Int, typedaddress: String) {
        if (typedaddress != "") {
            _otherData.value = _otherData.value.toMutableList()
                .also { it[id] = it[id].copy(address = typedaddress) }.toList()
        } else {
            _otherData.value =
                _otherData.value.toMutableList().also { it[id] = it[id].copy(address = "NIL") }
                    .toList()
        }
    }

    fun getOtherName(id: Int): String {
        if (id < _otherData.value.size) {
            return _otherData.value[id].name
        }
        return ""
    }

    fun getOtherAddress(id: Int): String {
        if (id < _otherData.value.size) {
            return _otherData.value[id].address
        }
        return ""
    }

    fun editSliderPos(pos: Float) {
        _sliderPosition.value = pos
        prefsMixin.setSearchRadius(pos.toInt())
    }

    fun editSelectedOptionText(text: String) {
        _selectedOptionText.value = text
        prefsMixin.setVehicle(
            when (text) {
                "Car" -> VehicleType.CAR
                "Motorcycle" -> VehicleType.MOTORCYCLE
                "Heavy Vehicle" -> VehicleType.HEAVY_VEHICLE
                else -> throw IllegalArgumentException("unknown vehicle type")
            }
        )
    }

    fun changeShownOther(index: Int) {
        _shownOther.value = index
    }

    fun getShownOther(): Int {
        return _shownOther.value
    }

    fun getSliderLowerBound(): Float {
        return _sliderLowerBound.value
    }

    fun getSliderUpperBound(): Float {
        return _sliderUpperBound.value
    }

    fun addLocation(name: String, address: String) {
        val newData = OtherItem(index, name, address)
        _otherData.value = _otherData.value + newData
        index++
    }

    fun onDismissDialog() {
        isDialogShown = false
        isOtherShown = false
        //Save our data into the god object
        updateSavedLocationList()
    }

    fun updateSavedLocationList(): List<StoredLocation> {
        val mutableList = mutableListOf<StoredLocation>()
        //Add the home data
        mutableList.add(
            StoredLocation(
                tag = homeData.value.name.trim(),
                name = homeData.value.location?.name?.trim() ?: "-",
                address = homeData.value.location?.address?.trim() ?: "-",
                epsg4326 = homeData.value.location?.epsg4326
                    ?: Coordinate.parse("+01.3888828+103.8767195/")
            )
        )
        mutableList.add(
            StoredLocation(
                tag = workData.value.name.trim(),
                name = workData.value.location?.name?.trim() ?: "-",
                address = workData.value.location?.address?.trim() ?: "-",
                epsg4326 = workData.value.location?.epsg4326
                    ?: Coordinate.parse("+01.3888828+103.8767195/")
            )
        )
        for (item in otherData.value) {
            if (item.name.isBlank() && item.address.isBlank()) continue
            mutableList.add(
                StoredLocation(
                    tag = item.name.trim(),
                    name = item.location?.name?.trim() ?: "-",
                    address = item.location?.address?.trim() ?: "-",
                    epsg4326 = item.location?.epsg4326 ?: Coordinate.parse(
                        "+01.3888828+103.8767195/"
                    )
                )
            )
        }

        prefsMixin.setCustomLocations(mutableList.distinctBy { it.tag })
        return mutableList
    }
}

private fun VehicleType.asOptionText() = when (this) {
    VehicleType.CAR -> "Car"
    VehicleType.MOTORCYCLE -> "Motorcycle"
    VehicleType.HEAVY_VEHICLE -> "Heavy Vehicle"
}