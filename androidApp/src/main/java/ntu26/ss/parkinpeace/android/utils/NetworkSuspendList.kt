package ntu26.ss.parkinpeace.android.utils

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.collectLatest
import ntu26.ss.parkinpeace.IOFlow
import ntu26.ss.parkinpeace.exclusiveBy

/**
 * DK whether this is a good idea
 * It will only request more elements from the API if the user scrolls past a certain threshold
 */
fun <T> networkSuspendList(
    scope: CoroutineScope, stream: Flow<IOFlow<T>>, prefetchLimit: Int = 25
): List<() -> T> {
    val real: MutableList<T> = mutableListOf()
    val dummy = mutableStateListOf<() -> T>()
    val job = Job()

    scope.launch {
        stream.collectLatest { latest ->
            val lock: Channel<Unit> = Channel(0)
            exclusiveBy(job, scope + Dispatchers.IO + CoroutineName("networkSuspendListWorker")) {
                var limit = prefetchLimit
                Log.d("networkSuspendList", "new query received, resetting")
                real.clear()
                dummy.clear()
                latest.collectIndexed { index, item ->
                    real.add(item)
                    if (limit <= index || index == latest.size - 1) {
                        dummy.addAll(mkDummyFn(
                            dummy.size, real.size - dummy.size, 10, real
                        ) {
                            if (real.size < latest.size && real.size < it + prefetchLimit) launch {
                                lock.receive()
                                Log.d(
                                    "networkSuspendList",
                                    "Unblocking to get more elements. dummy size is ${dummy.size}, current=$it, want = ${it + prefetchLimit}"
                                )
                            }
                        })
                        Log.d(
                            "networkSuspendList",
                            "added elements to dummy. dummy=${dummy.size} real=${real.size} max=${latest.size}"
                        )
                    }
                    if (index == latest.size - 1) {
                        Log.d("networkSuspendList", "fully served")
                        lock.close()
                    }
                    if (limit <= index) {
                        limit += prefetchLimit
                        if (!lock.isClosedForSend) {
                            Log.d("networkSuspendList", "suspending collect @ $index")
                            lock.send(Unit)
                            Log.d("networkSuspendList", "resumed collect @ $index")
                        }
                    }
                }
            }
        }
    }

    return dummy
}

private fun <T> mkDummyFn(
    start: Int, size: Int, check: Int, backingList: List<T>, block: (Int) -> Unit
): List<() -> T> = buildList {
    repeat(size) {
        add {
            if (it != 0 && it % check == 0) block(start + it)
            backingList[start + it]
        }
    }
}