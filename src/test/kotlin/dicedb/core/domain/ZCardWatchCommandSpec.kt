package dicedb.core.domain

import io.kotest.matchers.shouldBe
import kotlin.runCatching
import test.base.DiceDBSpec

class ZCardWatchCommandSpec : DiceDBSpec() {

    init {
        Given("A running DiceDB instance") {
            When("ZCARD.WATCH is called without a key argument") {
                val result = runCatching { client.fire(Command.Raw("ZCARD.WATCH")) }

                Then("It should return an argument error") { result.isFailure shouldBe true }
            }
        }
    }
}
