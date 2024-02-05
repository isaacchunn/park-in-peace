package ntu26.ss.parkinpeace.server.data.external

import ntu26.ss.parkinpeace.models.Lot
import ntu26.ss.parkinpeace.server.data.Ref

data class DatagovCarpark(
    val ref: Ref,
    val name: String,
    val epsg3414: String,
    val lots: List<Lot>
)

fun DatagovCarpark.asExternalCarparkSource(): ExternalCarparkSource = ExternalCarparkSource.Datagov(this)