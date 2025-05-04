package dicedb.core.domain

import io.kotest.matchers.shouldBe
import test.base.DiceDBSpec

class TypeCommandSpec : DiceDBSpec() {

    init {
        Given("A running DiceDB instance") {
            When("TYPE for non-existent key") {
                val type = client.fire(Command.Type("k1"))

                Then("It should return 'none'") { type.type shouldBe "none" }
            }

            When("TYPE for key with String value") {
                client.fire(Command.Set("k1", "v1"))
                val type = client.fire(Command.Type("k1"))

                Then("It should return 'string'") { type.type shouldBe "string" }
            }
        }
    }
}
