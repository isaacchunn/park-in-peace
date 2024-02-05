package ntu26.ss.parkinpeace

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext

data class IOFlow<T>(val size: Int, private val internal: Flow<T>) : Flow<T> by internal {
    companion object {
        fun <T> empty() = IOFlow<T>(0, flowOf())
    }
}

/**
 * Allows only a single instance of the request to be active.
 *
 * This is achieved by launching the request under the monitoring [job].
 *
 * When a new request is made, the previous request is cancelled by calling [kotlinx.coroutines.Job.cancelChildren]
 *
 * Thus, only one request can be active at a time under [job]
 *
 * The coroutine will be dispatched under [scope]
 *
 * @note Does not work for Flows!
 * @note Any tasks under the same job will be cancelled!
 */
suspend fun <T> exclusiveBy(
    job: Job,
    scope: CoroutineScope,
    block: suspend CoroutineScope.() -> T
): T {
    job.cancelChildren()
    return (scope + job).async(block = block).await()
}

/**
 * Allows only a single instance of the request to be active.
 *
 * This is achieved by launching the request under the monitoring [job].
 *
 * When a new request is made, the previous request is cancelled by calling [kotlinx.coroutines.Job.cancelChildren]
 *
 * Thus, only one request can be active at a time under [job]
 *
 * @note Does not work for Flows!
 * @note Any tasks under the same job will be cancelled!
 */
suspend fun <T> exclusiveBy(job: Job, block: suspend CoroutineScope.() -> T): T {
    job.cancelChildren()
    return withContext(job, block)
}

/**
 * Allows only a single instance of the request to be active.
 *
 * For requests that return [Flow]s, the emission is performed on the collecting thread/coroutine/job.
 *
 * This makes it not cancellable under [job] because we are not collecting it, but merely passing on
 * a reference to the Flow.
 *
 * Hence, normally the collector (usually the [androidx.lifecycle.ViewModel] that initiated the request)
 * is the one that is responsible for cancelling a [Flow].
 *
 * However, if we want to shift the responsibility away from the collector to us (the producer),
 * we need to make the emission happen on our terms and not the collector's terms.
 *
 * Converting the Flow to a SharedFlow does exactly that.
 *
 * @see [ntu26.ss.parkinpeace.android.api.onemap.CancellableOneMapApiImpl.search] for an example
 */
fun <T> IOFlow<T>.exclusiveBy(job: Job, scope: CoroutineScope): SharedFlow<T> {
    job.cancelChildren()
    return this.buffer(0).shareIn(scope + job, SharingStarted.WhileSubscribed(0, 0))
}