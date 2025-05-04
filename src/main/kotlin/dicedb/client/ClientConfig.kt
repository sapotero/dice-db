package dicedb.client

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class ClientConfig(
    val maxMsgSize: Int = 32 * 1024 * 1024,
    val retryDelay: Duration = 5000.milliseconds,
    val maxRetries: Int = 3
)