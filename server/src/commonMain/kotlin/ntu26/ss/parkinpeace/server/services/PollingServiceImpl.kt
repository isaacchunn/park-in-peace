package ntu26.ss.parkinpeace.server.services

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import ntu26.ss.parkinpeace.server.api.datagovsg.DatagovsgApi
import ntu26.ss.parkinpeace.server.api.datagovsg.getAllCarparks
import ntu26.ss.parkinpeace.server.api.datagovsg.getAvailabilities
import ntu26.ss.parkinpeace.server.api.lta.LtaApi
import ntu26.ss.parkinpeace.server.api.lta.getCarparksAndAvailabilities
import ntu26.ss.parkinpeace.server.api.ura.UraApi
import ntu26.ss.parkinpeace.server.api.ura.getAllCarparks
import ntu26.ss.parkinpeace.server.api.ura.getAvailabilities
import ntu26.ss.parkinpeace.server.data.external.*
import org.slf4j.LoggerFactory
import retrofit2.HttpException
import java.io.IOException
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class PollingServiceImpl(
    private val scope: CoroutineScope,
    private val uraApi: UraApi,
    private val ltaApi: LtaApi,
    private val datagovsgApi: DatagovsgApi,
    private val clock: Clock = Clock.System
) : PollingService {
    private val logger = LoggerFactory.getLogger(javaClass)

    override val uraCarparks: Flow<List<UraCarpark>> =
        scope.ticker(24.hours.inWholeSeconds, CoroutineName("Polling.Ura.Carparks"), clock)
            .watch(uraApi::getAllCarparks)

    override val uraVacancies: Flow<List<UraAvailability>> =
        scope.ticker(5.minutes.inWholeSeconds, CoroutineName("Polling.Ura.Vacancies"), clock)
            .watch(uraApi::getAvailabilities)

    override val ltaVacancies: Flow<List<LtaCarparkAvailability>> =
        scope.ticker(2.minutes.inWholeSeconds, CoroutineName("Polling.Lta.Vacancies"), clock)
            .watch(ltaApi::getCarparksAndAvailabilities)

    override val datagovCarparks: Flow<List<DatagovCarpark>> =
        scope.ticker(30.minutes.inWholeSeconds, CoroutineName("Polling.Dsg.Carparks"), clock)
            .watch(datagovsgApi::getAllCarparks)

    override val datagovVacancies: Flow<List<DatagovAvailability>> =
        scope.ticker(2.minutes.inWholeSeconds, CoroutineName("Polling.Dsg.Vacancies"), clock)
            .watch(datagovsgApi::getAvailabilities)

    private fun <T, R> Flow<T>.watch(api: suspend () -> R): Flow<R> =
        transform {
            try {
                emit(api())
            } catch (e: IOException) {
                logger.error("Unable to fetch upstream data", e)
            } catch (e: HttpException) {
                logger.error("Unable to fetch upstream data", e)
            } catch (e: Exception) {
                logger.error("[SEVERE] Unable to fetch upstream data", e)
            }
        }.flowOn(Dispatchers.IO)
}

/*
 * Copyright 2016-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 *
 * Adapted from https://github.com/Kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/src/channels/TickerChannels.kt#L62
 */
/**
 * @see kotlinx.coroutines.channels.ticker
 */
private fun CoroutineScope.ticker(
    delaySeconds: Long,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    clock: Clock = Clock.System
): SharedFlow<Unit> {
    val flow = MutableSharedFlow<Unit>(1, 1, BufferOverflow.DROP_OLDEST)

    launch(Dispatchers.Unconfined + coroutineContext) {
        var deadline = clock.now().epochSeconds
        while (true) {
            deadline += delaySeconds
            flow.emit(Unit)
            val now = clock.now().epochSeconds
            val nextDelay = (deadline - now).coerceAtLeast(0)
            if (nextDelay == 0L && delaySeconds != 0L) {
                val adjustedDelay = delaySeconds - (now - deadline) % delaySeconds
                deadline = now + adjustedDelay
                delay(adjustedDelay.seconds.inWholeMilliseconds)
            } else {
                delay(nextDelay.seconds.inWholeMilliseconds)
            }
        }
    }

    return flow
}

