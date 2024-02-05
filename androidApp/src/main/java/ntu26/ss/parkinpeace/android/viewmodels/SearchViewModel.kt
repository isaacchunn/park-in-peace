package ntu26.ss.parkinpeace.android.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import ntu26.ss.parkinpeace.IOFlow
import ntu26.ss.parkinpeace.android.services.LocationManager
import ntu26.ss.parkinpeace.android.services.LocationService
import ntu26.ss.parkinpeace.android.utils.networkSuspendList
import ntu26.ss.parkinpeace.exclusiveBy
import ntu26.ss.parkinpeace.models.Location

class SearchViewModel(private val historyMixin: HistoryMixin = MemoryDemoHistoryMixin) : ViewModel(),
    HistoryMixin by historyMixin {

    constructor() : this(MemoryDemoHistoryMixin)

    // TODO: Inject
    private val service: LocationService = LocationManager(viewModelScope)

    private var _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    val searchResults = _query.distinctUntilChanged(String::contentEquals)
        .debounce(250)
        .transformLatest { emit(searchAndUpdate(it)) }
        .let { networkSuspendList(viewModelScope, it) }

    private val job: Job = SupervisorJob()

    fun onSearchTextChange(text: String) {
        _query.value = text
    }

    fun setFocused(isFocused: Boolean) {
        _isSearching.value = isFocused
    }

    fun clearText(){
        _query.value = ""
        _isSearching.value = false
    }

    private suspend fun searchAndUpdate(query: String): IOFlow<Location> = when {
        query.isBlank() || query.length < 3 -> IOFlow.empty()
        else -> exclusiveBy(job) { service.resolve(query) }
    }

    fun updateHistory(carpark: Location){
        modifyHistory { it + carpark }
    }
}







