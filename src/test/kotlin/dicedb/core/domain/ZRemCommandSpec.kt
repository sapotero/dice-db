package dicedb.core.domain

import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
import test.base.DiceDBSpec

class ZRemCommandSpec : DiceDBSpec() {
    init {
        Given("A ZREM command") {
            When("called with no arguments or just a key") {
                Then("it should return an argument error") {
                    shouldThrowAny { client.fire(Command.Raw("ZREM")) }
                    shouldThrowAny { client.fire(Command.Raw("ZREM key")) }
                }
            }

            When("called on non-existent key") {
                val result = client.fire(Command.ZRem("nonExistingKey", listOf("member1")))
                Then("it should return 0") { result.count shouldBe 0 }
            }

            When("called on a non-sorted set key") {
                client.fire(Command.Set("key", "value"))
                Then("it should return a type error") {
                    shouldThrowAny { client.fire(Command.ZRem("key", listOf("member1"))) }
                }
            }

            When("removing multiple existing members from a sorted set") {
                client.fire(
                    Command.ZAdd("key1", listOf(1 to "member1", 2 to "member2", 3 to "member3"))
                )
                val result = client.fire(Command.ZRem("key1", listOf("member1", "member2")))
                Then("it should remove them and return count 2") { result.count shouldBe 2 }
            }
        }
    }
}
