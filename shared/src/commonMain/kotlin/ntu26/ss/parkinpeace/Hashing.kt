package ntu26.ss.parkinpeace

/**
 * Implementations should use platform-specific crypto libraries
 * to compute the SHA3-256 hash for data
 */
expect fun computeSHA3(data: String): String