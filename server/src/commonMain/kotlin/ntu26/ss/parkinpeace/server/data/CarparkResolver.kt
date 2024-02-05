package ntu26.ss.parkinpeace.server.data

interface CarparkResolver {
    fun isResolved(cp: RawDbCarpark): Boolean
    suspend fun resolve(cp: RawDbCarpark): RawDbCarpark
}