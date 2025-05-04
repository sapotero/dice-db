package dicedb.core.domain

import io.kotest.assertions.throwables.shouldThrow
import test.base.DiceDBSpec

class HGetWatchCommandSpec : DiceDBSpec() {
    init {
        Given("An active DiceDB client") {
            When("HGET.WATCH is called with no arguments") {
                Then("It should return an argument error") {
                    shouldThrow<IllegalStateException> { client.fire(Command.Raw("HGET.WATCH")) }
                }
            }

            When("HGET.WATCH is called with only key argument") {
                Then("It should return an argument error") {
                    shouldThrow<IllegalStateException> { client.fire(Command.Raw("HGET.WATCH k1")) }
                }
            }

            When("HGET.WATCH is called with key and field") {
                Then("It should return OK") { client.fire(Command.HGetWatch("k1", "f1")) }
            }
        }
    }
}
