package ntu26.ss.parkinpeace.server.services

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import ntu26.ss.parkinpeace.server.data.external.asExternalAvailabilitySource
import ntu26.ss.parkinpeace.server.data.external.asExternalCarparkSource
import ntu26.ss.parkinpeace.server.data.repositories.AvailabilityRepository
import ntu26.ss.parkinpeace.server.data.repositories.CarparkRepository
import kotlin.time.Duration.Companion.seconds

class DataManagerImpl(
    private val scope: CoroutineScope,
    private val pollingService: PollingService,
    private val carparkRepository: CarparkRepository,
    private val availabilityRepository: AvailabilityRepository,
) : DataManager {
    override fun start() {
        val carparks = let {
            val ura = pollingService.uraCarparks.map { it.map { it.asExternalCarparkSource() } }
            val lta = pollingService.ltaVacancies.map { it.map { it.asExternalCarparkSource() } }
            val dsg = pollingService.datagovCarparks.map { it.map { it.asExternalCarparkSource() } }
            listOf(ura, lta, dsg).merge()
        }
        val availabilities = let {
            // val ura = pollingService.uraVacancies.map { it.map { it.asExternalAvailabilitySource() } }
            val lta =
                pollingService.ltaVacancies.map { it.map { it.asExternalAvailabilitySource() } }
            val dsg =
                pollingService.datagovVacancies.map { it.map { it.asExternalAvailabilitySource() } }
            // listOf(ura, lta).merge()
            listOf(dsg, lta).merge()
        }
        scope.launch {
            supervisorScope {
                launch(Dispatchers.Default + CoroutineName("CarparkRepositoryObserver")) {
                    carparkRepository.observe(carparks)
                }
                delay(30.seconds)
                launch(Dispatchers.Default + CoroutineName("AvailabilityRepositoryObserver")) {
                    availabilityRepository.observe(availabilities)
                }
            }
        }
    }
}