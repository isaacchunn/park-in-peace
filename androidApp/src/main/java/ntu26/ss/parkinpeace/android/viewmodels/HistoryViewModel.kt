package ntu26.ss.parkinpeace.android.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ntu26.ss.parkinpeace.android.data.HistoryDB
import ntu26.ss.parkinpeace.android.data.SearchHistoryDB
import ntu26.ss.parkinpeace.android.data.SearchHistoryDao
import ntu26.ss.parkinpeace.android.data.VisitedLocationsDao
import ntu26.ss.parkinpeace.android.models.StoredLocation

class HistoryViewModel(application: Application) : AndroidViewModel(application){

    @SuppressLint("StaticFieldLeak")
    val applicationContext = this.getApplication<Application>().applicationContext

    //db for visited locations
    val dao: VisitedLocationsDao =
        HistoryDB.buildDatabase(application).dao

    //db for recent searchs
    val search_dao: SearchHistoryDao =
        SearchHistoryDB.buildDatabase(application).dao

    private val _state = MutableStateFlow<List<StoredLocation>>(emptyList())
    val state: StateFlow<List<StoredLocation>> = _state

    private val _recentSearch = MutableStateFlow<List<StoredLocation>>(emptyList())
    val recentSearch: StateFlow<List<StoredLocation>> = _recentSearch

    suspend fun getHistory() {
        withContext(Dispatchers.IO) {
            //dao.deleteAll()
            val locations = dao.getLocations().reversed()
            _state.value = locations
        }
    }

    suspend fun getSearchHistory() {
        withContext(Dispatchers.IO) {
            //dao.deleteAll()
            val locations = search_dao.getLocations().reversed()
            _recentSearch.value = locations
        }
    }

    fun deleteSearchHistory(location: StoredLocation) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                search_dao.deleteLocation(location)
                getSearchHistory()
            }
        }
    }

    fun deleteVisitedHistory(location: StoredLocation) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dao.deleteLocation(location)
                getHistory()
            }
        }
    }

    fun updateHistory(location: StoredLocation) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dao.insertLocation(location)
                getHistory()
            }
        }
    }

    fun updateSearchHistory(location: StoredLocation) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                search_dao.insertLocation(location)
                getSearchHistory()
            }
        }
    }

    fun purgeSearchHistory(){
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                search_dao.deleteAll()
                getSearchHistory()
            }
        }
    }

    fun purgeLocationHistory(){
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dao.deleteAll()
                getHistory()
            }
        }
    }
}