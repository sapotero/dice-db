package dicedb.example.leaderboard

import dicedb.core.domain.Command
import dicedb.core.proto.Response
import dicedb.core.proto.ZElement
import kotlinx.coroutines.runBlocking

val players = listOf("Alice", "Bob", "Charlie", "Dora", "Evan", "Fay", "Gina")

fun main(args: Array<String>): Unit = runBlocking {
    LeaderboardService(true).subscribe(Command.ZRangeWatch("game:scores", 1, 5)) {
        res: Response.ZRANGERes ->
        val elements = res.elements
        if (elements.isNotEmpty()) displayLeaderboard(elements)
    }
}

fun displayLeaderboard(leaderboard: List<ZElement>) {
    // clear screen
    print("\u001b[H\u001b[2J")

    println("Rank  Score  Player")
    println("------------------")
    for (element in leaderboard) {
        println("%2d.   %4d   %s".format(element.rank, element.score, element.member))
    }
    println("------------------")
}
