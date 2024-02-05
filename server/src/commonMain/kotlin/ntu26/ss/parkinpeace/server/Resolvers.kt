package ntu26.ss.parkinpeace.server

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import ntu26.ss.parkinpeace.models.Coordinate
import ntu26.ss.parkinpeace.server.data.CarparkResolver
import ntu26.ss.parkinpeace.server.data.RawDbCarpark
import ntu26.ss.parkinpeace.server.services.LocationService

fun LocationService.asCarparkResolver(): CarparkResolver {
    return object : CarparkResolver {
        override fun isResolved(cp: RawDbCarpark): Boolean =
            cp.epsg4326 != null && cp.address != null

        override suspend fun resolve(cp: RawDbCarpark): RawDbCarpark {
            return coroutineScope {
                val epsg4326P = async(Dispatchers.IO + CoroutineName("OneMapApi@convert")) {
                    cp.epsg4326 ?: this@asCarparkResolver.convertEpsg3414to4326(cp.epsg3414)
                }
                val addressP = async(Dispatchers.IO + CoroutineName("OneMapApi@address")) {
                    when (cp.address) {
                        null -> when (cp.epsg4326) {
                            null -> this@asCarparkResolver.lookupAddress(cp.epsg3414).firstOrNull()
                            else -> this@asCarparkResolver.lookupAddress(cp.epsg4326).firstOrNull()
                        }

                        else -> cp.address
                    }

                }
                val result = awaitAll(epsg4326P, addressP)
                val epsg4326 = result[0] as Coordinate
                val address = result[1] as? String?
                cp.copy(epsg4326 = epsg4326, address = address)
            }
        }

    }
}