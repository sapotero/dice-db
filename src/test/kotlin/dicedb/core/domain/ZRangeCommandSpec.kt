package dicedb.core.domain

import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
import test.base.DiceDBSpec

class ZRangeCommandSpec : DiceDBSpec() {
    init {
        Given("A sorted set") {
            When("ZRANGE is called with insufficient arguments") {
                Then("It should return argument count error") {
                    shouldThrowAny { client.fire(Command.Raw("ZRANGE")) }
                    shouldThrowAny { client.fire(Command.Raw("ZRANGE key")) }
                    shouldThrowAny { client.fire(Command.Raw("ZRANGE key 1")) }
                }
            }

            When("ZRANGE is called with non-numeric start/stop") {
                Then("It should fail with type error") {
                    shouldThrowAny { client.fire(Command.Raw("ZRANGE key a b")) }
                    shouldThrowAny { client.fire(Command.Raw("ZRANGE key 1 b")) }
                }
            }

            When("ZRANGE is called on non-existent key") {
                val result = client.fire(Command.ZRange("missing", 1, 2))
                Then("It should return empty list") { result.elements shouldBe emptyList() }
            }

            When("ZRANGE is called on wrong type key") {
                client.fire(Command.Set("strKey", "value"))
                Then("It should fail with type error") {
                    shouldThrowAny { client.fire(Command.ZRange("strKey", 1, 2)) }
                }
            }

            When("ZRANGE is called with valid sorted set and range") {
                client.fire(Command.ZAdd("z1", listOf(1 to "mem1", 2 to "mem2")))
                val result = client.fire(Command.ZRange("z1", 0, 0))
                Then("It should return the first element") {
                    result.elements.map { it.member } shouldBe listOf("mem1")
                    result.elements.map { it.score } shouldBe listOf(1)
                }
            }
        }
    }
}
