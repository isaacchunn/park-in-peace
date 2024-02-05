package ntu26.ss.parkinpeace

import java.security.MessageDigest

@OptIn(ExperimentalStdlibApi::class)
actual fun computeSHA3(data: String): String {
    val digest = MessageDigest.getInstance("SHA3-256")
    val hashBytes = digest.digest(data.toByteArray(Charsets.UTF_8))
    return hashBytes.toHexString()
}