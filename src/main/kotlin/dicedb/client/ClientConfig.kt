package dicedb.client

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class ClientConfig(
    val maxMessageSize: Int = 32 * 1024 * 1024,
    val retryDelay: Duration = 5000.milliseconds,
    val maxAttempts: Int = 3,
)
