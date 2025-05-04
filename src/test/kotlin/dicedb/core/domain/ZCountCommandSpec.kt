package dicedb.core.domain

import io.kotest.matchers.shouldBe
import test.base.DiceDBSpec

class ZCountCommandSpec : DiceDBSpec() {
    init {
        Given("A running DiceDB instance") {
            When("ZCOUNT is called on a sorted set with elements in range") {
                client.fire(
                    Command.ZAdd("myzset", listOf(5 to "member1", 7 to "member2", 9 to "member3"))
                )
                val count = client.fire(Command.ZCount("myzset", 5, 10))

                Then("It should return 3") { count.count shouldBe 3 }
            }

            When("ZCOUNT is called with an empty range") {
                client.fire(
                    Command.ZAdd("s100", listOf(5 to "member1", 7 to "member2", 9 to "member3"))
                )
                val count = client.fire(Command.ZCount("s100", 11, 15))

                Then("It should return 0") { count.count shouldBe 0 }
            }

            When("ZCOUNT is called on a non-existent key") {
                val count = client.fire(Command.ZCount("s101", 1, 5))

                Then("It should return 0") { count.count shouldBe 0 }
            }

            When("ZCOUNT is called with duplicate scores") {
                client.fire(
                    Command.ZAdd(
                        "s3",
                        listOf(5 to "member1", 5 to "member2", 5 to "member3", 7 to "member4"),
                    )
                )
                val count = client.fire(Command.ZCount("s3", 5, 5))

                Then("It should return 3") { count.count shouldBe 3 }
            }

            When("ZCOUNT is called with range [7, 7]") {
                client.fire(
                    Command.ZAdd("s2", listOf(5 to "member1", 7 to "member2", 9 to "member3"))
                )
                val count = client.fire(Command.ZCount("s2", 7, 7))

                Then("It should return 1") { count.count shouldBe 1 }
            }
        }
    }
}
