package ntu26.ss.parkinpeace.android.viewmodels

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.launch
import ntu26.ss.parkinpeace.IOFlow
import ntu26.ss.parkinpeace.android.models.CarparkAvailability
import ntu26.ss.parkinpeace.android.models.NamedFilter
import ntu26.ss.parkinpeace.android.models.SortingStrategy
import ntu26.ss.parkinpeace.android.services.NavigationService
import ntu26.ss.parkinpeace.android.utils.of
import ntu26.ss.parkinpeace.models.Coordinate
import ntu26.ss.parkinpeace.models.Location
import ntu26.ss.parkinpeace.models.of
import java.io.IOException
import java.math.BigDecimal


sealed interface MapUIState {
    object Success : MapUIState
    object Error : MapUIState
    object Loading : MapUIState
}

class MapViewModel(application: Application, private val mapMixin: MapMixin = MemoryDemoMixin) :
    AndroidViewModel(application), MapMixin by mapMixin {
    constructor(application: Application) : this(application, MemoryDemoMixin)

    private var hasInit = false
    private val _mapUIState = MutableStateFlow<MapUIState>(MapUIState.Loading)
    val mapUIState = _mapUIState.asStateFlow()

    // user coordinates state
    private val _userCoordinate = MutableStateFlow(INITIAL_LOCATION)
    val userCoordinate = _userCoordinate.asStateFlow()

    private val _isRecentering = MutableStateFlow(false)
    val isRecentering = _isRecentering.asStateFlow()

    // map coordinates state
    private val _coordinate = MutableStateFlow<Coordinate?>(null)
    val coordinate: StateFlow<Coordinate?> = _coordinate

    // carpark info state
    private var _carpark = MutableStateFlow<CarparkAvailability?>(null)
    val carpark: MutableStateFlow<CarparkAvailability?> = _carpark

    // info UI state when prompt clicked
    private val _displayState = MutableStateFlow(false)
    val displayState: StateFlow<Boolean> = _displayState

    // prompt UI state when carpark icon clicked
    private val _promptState = MutableStateFlow(false)
    val promptState: StateFlow<Boolean> = _promptState

    // carpark info state of popup
    private val _displayCarpark = MutableStateFlow<CarparkAvailability?>(null)
    val displayCarpark: StateFlow<CarparkAvailability?> = _displayCarpark

    // map camera state
    private val _mapCoords = MutableStateFlow(userCoordinate.value)
    val mapCoords = _mapCoords.asStateFlow()

    private val _btmSheetState = MutableStateFlow(false)
    val btmSheetState: StateFlow<Boolean> = _btmSheetState

    private val service: NavigationService get() = NavigationService.single

    private var mapOffset = EPSILON

    fun init() {
        Log.d(javaClass.simpleName, "init@$this")
        if (!hasInit) {
            Log.d(javaClass.simpleName, "init2")
            hasInit = true
            getUserLocation()
            getCarparkDetails()
        }
    }

    fun recenter() {
        Log.d(javaClass.simpleName, "recenter")
        _isRecentering.value = true
        getUserLocation()
        moveMapCamera(_userCoordinate.value)
    }

    fun recentered() {
        _isRecentering.value = false
    }

    fun gotoLocation(location: Location) = gotoLocation(location.epsg4326)

    fun gotoLocation(coords: Coordinate) {
        Log.d(javaClass.simpleName, "goToLocation $coords")
        _coordinate.value = coords
        moveMapCamera(coords)
        getCarparkDetails()
    }

    fun moveMapCamera(coords: Coordinate) {
        Log.d(javaClass.simpleName, "moveMapCamera $coords")
        mapOffset *= BigDecimal(-1)
        _mapCoords.value =
            if (_mapCoords.value == coords) coords.copy(latitude = coords.latitude + mapOffset) else coords
    }

    private fun Context.hasPermission(permission: String) =
        ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    private fun requestLocationPermissions(): Boolean {
        val applicationContext = this.getApplication<Application>().applicationContext

        val coarse = applicationContext.hasPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
        val fine = applicationContext.hasPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)

        if (coarse || fine) return true

        ActivityCompat.requestPermissions(
            applicationContext as Activity,
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            100
        )
        return false
    }

    @SuppressLint("MissingPermission")
    fun getUserLocation() {
        Log.d(javaClass.simpleName, "getUserLocation")
        val fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this.getApplication<Application>().applicationContext)

        if (requestLocationPermissions()) {
            val location =
                fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            location.addOnSuccessListener {
                if (it != null) {
                    Log.d(javaClass.simpleName, "location.addOnSuccessListener")
                    _userCoordinate.value = Coordinate.of(it)
                    _mapCoords.value = Coordinate.of(it)
                    _mapUIState.value = MapUIState.Success
                } else {
                    // userCoordinate = INITIAL_LOCATION
                }
            }
        }
    }

    private fun updateNearbyCarparks(nearbyCarparksIO: IOFlow<CarparkAvailability>) {
        Log.d(javaClass.simpleName, "updateNearbyCarparks")
        viewModelScope.launch(Dispatchers.IO) {
            nearbyCarparksIO.runningFold(listOf<CarparkAvailability>()) { acc, it -> acc + it }
                .collect { mapMixin.setNearbyCarparks(it) }
        }
    }

    private fun guard(block: suspend CoroutineScope.() -> Unit) {
        if (service.authenticated) {
            viewModelScope.launch(block = block)
        } else {
            onAuthFailed()
        }
    }

    private fun onAuthFailed() {
        _mapUIState.value = MapUIState.Error
    }

    fun getCarparkDetails(): Unit = guard {
        Log.d(javaClass.simpleName, "getCarparkDetails")
        try {
            val current = _userCoordinate.value
            val destination = _coordinate.value ?: return@guard
            val searchRadius = searchRadius.value
            val vehicle = vehicle.value
            Log.d(javaClass.simpleName, "getCarparkDetails 1")
            val details = service.queryNearby(
                origin = current,
                destination = destination,
                options = NavigationService.QueryOptions(
                    searchRadius = searchRadius,
                    limit = 20,
                    vehType = vehicle,
                    computeDistance = true,
                    computeTravelTime = true,
                    computeCarparkDetails = true
                )
            )
            Log.d(javaClass.simpleName, "getCarparkDetails 2")
            updateNearbyCarparks(details)
            Log.d(javaClass.simpleName, "getCarparkDetails 3")
            _mapUIState.value = MapUIState.Success
        } catch (e: IOException) {
            e.printStackTrace()
            _mapUIState.value = MapUIState.Error
        }
    }

    //    fun display(carparkInfo: CarparkAvailability) {
    //        if(carparkInfo.info != null) {
    //            _promptState.value = false
    //            delay(3000)
    //            moveMapCamera(carparkInfo.info.epsg4326)
    //            _displayCarpark.value = carparkInfo
    //            _promptState.value = true
    //        }
    //    }
    fun display(carparkInfo: CarparkAvailability) {
        if (carparkInfo.info != null) {
            Log.d(javaClass.simpleName, "display carparkInfo=$carparkInfo")
            // Set prompt to false to hide it

            // Launch a coroutine to delay for 3000 milliseconds
            viewModelScope.launch {
                _promptState.value = false
                // Move map camera and update carpark information
                moveMapCamera(carparkInfo.info.epsg4326)
                _displayCarpark.value = carparkInfo
                delay(50)
                _promptState.value = true
            }
        }
    }

    fun hide() {
        //_displayCarpark.value = null
        _displayState.value = false
    }
    fun togglePrompt() {
        _promptState.value = !_promptState.value
    }

    fun togglePopup() {
        _displayState.value = !_displayState.value
    }

    fun toggleBottomSheet() {
        _btmSheetState.value = !_btmSheetState.value
    }

    fun onSortStrategyChanged(sortingStrategy: SortingStrategy) {
        mapMixin.setSortingStrategy(sortingStrategy)
    }

    fun onFilterChanged(namedFilter: NamedFilter) {
        mapMixin.modifyFilters { it.updated(namedFilter) }
    }

    fun redirectGoogleMaps() {
        val appContext = getApplication<Application>()
        // If the display carpark value is null then return
        _displayCarpark.value?.info?.let { carparkInfo ->
            val carparkCoords = carparkInfo.epsg4326
            // Create a uri builder
            val builder = Uri.Builder()
                .scheme("https")
                .authority("www.google.com")
                .appendPath("maps")
                .appendPath("dir")
                .appendPath("")
                .appendQueryParameter("api", "1")
                .appendQueryParameter(
                    "destination",
                    listOf(carparkCoords.latitude, carparkCoords.longitude).joinToString(",")
                )
            val url = builder.build().toString()
            Log.d("Directions", url)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            // Needed as we are not starting it in main activity context...
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            // uses the package to start in gmaps application if the user has it
            intent.setPackage("com.google.android.apps.maps")
            // Try and throw exception if user does not have a gmap installed.
            try {
                appContext.startActivity(intent)
            } catch (ex: ActivityNotFoundException) {
                try {
                    // Starts it in browser instead of maps
                    val unrestrictedIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    unrestrictedIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    appContext.startActivity(unrestrictedIntent)
                } catch (ex: ActivityNotFoundException) {
                    Toast.makeText(appContext, "Please install a maps application!", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }

    }

    fun logVisit() {

    }

    companion object {
        private val EPSILON = 1e-6.toBigDecimal()
        val INITIAL_LOCATION = Coordinate.of(1.2966, 103.7764)
    }
}