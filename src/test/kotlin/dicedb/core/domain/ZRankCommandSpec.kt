package dicedb.core.domain

import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
import test.base.DiceDBSpec

class ZRankCommandSpec : DiceDBSpec() {
    init {
        Given("A sorted set") {
            When("ZRANK is called with existing member") {
                client.fire(
                    Command.ZAdd("users1", listOf(20 to "bob", 10 to "alice", 30 to "charlie"))
                )
                val result = client.fire(Command.ZRank("users1", "bob"))
                Then("It should return the correct rank (by score)") {
                    result.element.rank shouldBe 2
                }
            }

            When("ZRANK is called with non-existing member") {
                client.fire(
                    Command.ZAdd("users2", listOf(20 to "bob", 10 to "alice", 30 to "charlie"))
                )
                val result = client.fire(Command.ZRank("users2", "daniel"))
                Then("It should return 0 for missing member") { result.element.rank shouldBe 0 }
            }

            When("ZRANK is called on non-existent key") {
                val result = client.fire(Command.ZRank("nonexisting", "member1"))
                Then("It should return 0") { result.element.rank shouldBe 0 }
            }

            When("ZRANK is called with wrong number of arguments") {
                Then("It should fail with argument error") {
                    shouldThrowAny { client.fire(Command.Raw("ZRANK key")) }
                }
            }

            When("ZRANK is called with extra arguments or invalid syntax") {
                Then("It should fail with argument error") {
                    shouldThrowAny { client.fire(Command.Raw("ZRANK key member1 INVALID_OPTION")) }
                    shouldThrowAny { client.fire(Command.Raw("ZRANK key member1 member2")) }
                }
            }
        }
    }
}
