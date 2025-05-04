package dicedb.core.domain

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import test.base.DiceDBSpec

class GetWatchCommandSpec : DiceDBSpec() {

    init {
        Given("a client connected to DiceDB") {
            When("GET.WATCH is called without a key argument") {
                Then("it should return an argument count error") {
                    runBlocking {
                        val ex =
                            runCatching { client.fire(Command.Raw("GET.WATCH")) }.exceptionOrNull()

                        ex?.message shouldBe
                            "Command failed: wrong number of arguments for 'GET.WATCH' command"
                    }
                }
            }
        }
    }
}
