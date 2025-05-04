package dicedb.core.domain

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource
import kotlinx.coroutines.delay

class Retrier(
    private val maxRetries: Int = 3,
    private val retryDelay: Duration = 500.milliseconds,
    private val retryOn: (Throwable) -> Boolean = { true },
) {
    suspend fun <T> runWithRetry(
        onFailure: ((Throwable) -> Unit)? = null,
        block: suspend () -> T,
    ): T {
        val timeSource = TimeSource.Monotonic
        var attempt = 0
        var lastError: Throwable? = null

        while (attempt < maxRetries) {
            try {
                return block()
            } catch (e: Throwable) {
                if (!retryOn(e)) throw e
                lastError = e
                onFailure?.invoke(e)
                delay(retryDelay)
                attempt++
            }
        }

        throw lastError ?: IllegalStateException("Retrier failed without throwing an exception")
    }
}
