package dicedb.example.leaderboard

import dicedb.client.Client
import dicedb.core.domain.Command
import dicedb.core.proto.Response
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.time.Duration.Companion.milliseconds

class LeaderboardService(
    private val launchUpdate: Boolean = false,
    private val updateTimeout: Long = 500
){
    private val client: Client = Client("localhost", 7379)
    
    init {
        GlobalScope.launch {
            if (launchUpdate) {
                launchUpdate()
            }
        }
    }
    
    private suspend fun launchUpdate() =
        coroutineScope {
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
            }.await()
        }
    
    private suspend fun updateScore(player: String, score: Int) =
        client.fire(Command.ZAdd("game:scores", score, player))
    
    suspend fun subscribe(command: Command<*>, onUpdate: (Response.ZRANGERes) -> Unit) {
        client.watchFlow(command)
            .collect { msg ->
                msg.zrangeRes?.let {
                    onUpdate(it)
                } ?: println("Ignored non-ZRANGE response: $msg")
            }
    }
}
