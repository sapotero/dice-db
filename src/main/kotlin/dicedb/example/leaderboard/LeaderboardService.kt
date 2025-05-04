package dicedb.example.leaderboard

import dicedb.client.Client
import dicedb.core.domain.Command
import dicedb.core.proto.Response
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LeaderboardService(
    private val launchUpdate: Boolean = false,
    private val updateTimeout: Long = 500,
) {
    val client: Client = Client("localhost", 7379)

    init {
        GlobalScope.launch {
            if (launchUpdate) {
                launchUpdate()
            }
        }
    }

    private suspend fun launchUpdate() = coroutineScope {
        async {
                delay(updateTimeout)
                if (launchUpdate) {
                    while (true) {
                        val player = players.random()
                        val score = Random.nextInt(1..100)
                        updateScore(player, score)
                        delay(updateTimeout.milliseconds)
                    }
                }
            }
            .await()
    }

    private suspend fun updateScore(player: String, score: Int) =
        client.fire(Command.ZAdd("game:scores", listOf(score to player)))

    suspend inline fun <reified R : Response> subscribe(
        command: Command<R>,
        crossinline onUpdate: (R) -> Unit,
    ) = client.watchFlow(command).collect { msg -> onUpdate(msg) }
}
