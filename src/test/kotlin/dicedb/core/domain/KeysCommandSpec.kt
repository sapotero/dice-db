package dicedb.core.domain

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import test.base.DiceDBSpec

class KeysCommandSpec : DiceDBSpec() {

    init {
        Given("A running DiceDB instance") {
            When("KEYS is called with multiple matching keys") {
                val r1 = client.fire(Command.Set("k", "v"))
                val r2 = client.fire(Command.Set("k1", "v1"))
                val keys = client.fire(Command.Keys("k*"))

                Then("It should return all matching keys") {
                    keys.keys shouldContainAll listOf("k", "k1")
                }
            }

            When("KEYS is called with no matching keys") {
                val keys = client.fire(Command.Keys("a*"))

                Then("It should return an empty list") { keys.keys.shouldBeEmpty() }
            }

            When("KEYS is called with a single-character wildcard") {
                client.fire(Command.Set("k1", "v1"))
                client.fire(Command.Set("k2", "v2"))
                client.fire(Command.Set("ka", "va"))
                val keys = client.fire(Command.Keys("k?"))

                Then("It should return all 2-character keys starting with 'k'") {
                    keys.keys shouldContainAll listOf("k1", "k2", "ka")
                }
            }

            When("KEYS is called with a single specific key") {
                val r = client.fire(Command.Set("unique_key", "value"))
                val keys = client.fire(Command.Keys("unique*"))

                Then("It should return the matching key") {
                    keys.keys shouldBe listOf("unique_key")
                }
            }
        }
    }
}
