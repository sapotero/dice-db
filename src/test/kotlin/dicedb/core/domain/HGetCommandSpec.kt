package dicedb.core.domain

import io.kotest.matchers.shouldBe
import test.base.DiceDBSpec

class HGetCommandSpec : DiceDBSpec() {
    init {
        Given("HGET command") {
            Then("Get Value for Field stored at Hash Key") {
                client.fire(Command.HSet("k", listOf("f", "1"))).count shouldBe 1
                client.fire(Command.HGet("k", "f")).value shouldBe "1"
            }
        }
    }
}
