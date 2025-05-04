package dicedb.core.domain

import io.kotest.matchers.shouldBe
import kotlin.runCatching
import test.base.DiceDBSpec

class ZCardCommandSpec : DiceDBSpec() {

    init {
        Given("A running DiceDB instance") {
            When("ZCARD is called with wrong number of arguments") {
                val r1 = runCatching { client.fire(Command.Raw("ZCARD")) }
                val r2 = runCatching { client.fire(Command.Raw("ZCARD myzset more args")) }

                Then("It should return argument errors") {
                    r1.isFailure shouldBe true
                    r2.isFailure shouldBe true
                }
            }

            When("ZCARD is called on a non-zset key") {
                client.fire(Command.Set("string_key", "string_value"))
                val result = runCatching { client.fire(Command.ZCard("string_key")) }

                Then("It should return wrong type error") { result.isFailure shouldBe true }
            }

            When("ZCARD is called on a nonexistent sorted set") {
                client.fire(Command.ZAdd("myzset", listOf(1 to "one")))
                val result = client.fire(Command.ZCard("wrong_myzset"))

                Then("It should return 0") { result.count shouldBe 0 }
            }

            When("ZCARD is called on a sorted set with one element") {
                client.fire(Command.ZAdd("u2", listOf(1 to "one")))
                val result = client.fire(Command.ZCard("u2"))

                Then("It should return 1") { result.count shouldBe 1 }
            }

            When("ZCARD is used to track changes in a sorted set") {
                client.fire(Command.ZAdd("u3", listOf(1 to "one")))
                client.fire(Command.ZAdd("u3", listOf(2 to "two")))
                val c1 = client.fire(Command.ZCard("u3")).count
                client.fire(Command.ZAdd("u3", listOf(3 to "three")))
                val c2 = client.fire(Command.ZCard("u3")).count
                client.fire(Command.ZRem("u3", listOf("two")))
                val c3 = client.fire(Command.ZCard("u3")).count

                Then("It should reflect the correct element count") {
                    c1 shouldBe 2
                    c2 shouldBe 3
                    c3 shouldBe 2
                }
            }
        }
    }
}
