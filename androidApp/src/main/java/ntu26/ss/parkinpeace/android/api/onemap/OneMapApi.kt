package ntu26.ss.parkinpeace.android.api.onemap

import ntu26.ss.parkinpeace.IOFlow
import ntu26.ss.parkinpeace.models.Coordinate
import ntu26.ss.parkinpeace.models.Location

interface OneMapApi {
    data class SearchResult(
        override val name: String,
        override val address: String,
        override val epsg4326: Coordinate
    ) : Location

    suspend fun search(query: String): IOFlow<Location>
}