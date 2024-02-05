package ntu26.ss.parkinpeace.server.data.external

import ntu26.ss.parkinpeace.models.Lot

data class UraCarpark(
    val uraId: String,
    val name: String,
    val epsg3414: String?,
    val lots: List<Lot>
)

fun UraCarpark.asExternalCarparkSource(): ExternalCarparkSource = ExternalCarparkSource.Ura(this)