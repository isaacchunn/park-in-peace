package ntu26.ss.parkinpeace.server.services

import kotlinx.coroutines.flow.Flow
import ntu26.ss.parkinpeace.server.data.external.*

interface PollingService {
    val uraCarparks: Flow<List<UraCarpark>>
    val uraVacancies: Flow<List<UraAvailability>>
    val ltaVacancies: Flow<List<LtaCarparkAvailability>>
    val datagovCarparks: Flow<List<DatagovCarpark>>
    val datagovVacancies: Flow<List<DatagovAvailability>>
}