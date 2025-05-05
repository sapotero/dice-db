package dicedb.core.domain

import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield

/**
 * Utility for retrying a suspending [block] with delay and retry condition.
 *
 * @param maxAttempts Maximum number of attempts (must be ≥ 1).
 * @param retryDelay Delay between retries. Default is 500 milliseconds.
 * @param shouldRetry Predicate to determine if retry should occur on given [Throwable].
 */
class Retrier(
    private val maxAttempts: Int = DEFAULT_MAX_ATTEMPTS,
    private val retryDelay: Duration = DEFAULT_RETRY_DELAY,
    private val shouldRetry: (Throwable) -> Boolean = { true },
) {
    companion object {
        private const val DEFAULT_MAX_ATTEMPTS = 3
        private val DEFAULT_RETRY_DELAY = 500.milliseconds
    }

    /**
     * Runs [block] and retries on failure based on [shouldRetry], up to [maxAttempts].
     *
     * @param onFailure Callback invoked on each failure before retrying, with the exception and
     *   attempt index.
     * @return The successful result of [block], or throws the last error if all attempts fail.
     */
    suspend fun <T> run(block: suspend () -> T): T {
        require(maxAttempts > 0) { "maxAttempts must be ≥ 1" }

        for (attempt in 0..maxAttempts) {
            try {
                return block()
            } catch (e: Throwable) {
                if (e is CancellationException) throw e
                if (!shouldRetry(e) || attempt == maxAttempts - 1) throw e
                delay(retryDelay)
                yield()
            }
        }

        error("Unreachable: loop must either return or throw")
    }
}
