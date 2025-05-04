package dicedb.core.domain

import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
import test.base.DiceDBSpec

class ZPopMaxCommandSpec : DiceDBSpec() {
    init {
        Given("A sorted set with various members") {
            When("ZPOPMAX is called on a non-existent key") {
                val res = client.fire(Command.ZPopMax("nonexistent"))
                Then("It should return an empty list") { res.elements shouldBe emptyList() }
            }

            When("ZPOPMAX is called on a string key") {
                client.fire(Command.Set("strkey", "value"))
                Then("It should fail with a type error") {
                    shouldThrowAny { client.fire(Command.ZPopMax("strkey")) }
                }
            }

            When("ZPOPMAX is called without count") {
                client.fire(Command.ZAdd("zset", listOf(1 to "a", 2 to "b", 3 to "c")))
                val popped = client.fire(Command.ZPopMax("zset"))
                Then("It should return the element with the highest score") {
                    popped.elements.map { it.member } shouldBe listOf("c")
                    popped.elements.map { it.score } shouldBe listOf(3)
                }
            }

            When("ZPOPMAX is called with count = 2") {
                client.fire(Command.ZAdd("zset2", listOf(1 to "x", 2 to "y", 3 to "z")))
                val popped = client.fire(Command.ZPopMax("zset2", count = 2))
                Then("It should return two elements") {
                    popped.elements.map { it.member } shouldBe listOf("z", "y")
                    popped.elements.map { it.score } shouldBe listOf(3, 2)
                }
            }

            When("ZPOPMAX is called with count > set size") {
                client.fire(Command.ZAdd("zset3", listOf(10 to "m1", 20 to "m2")))
                val popped = client.fire(Command.ZPopMax("zset3", count = 10))
                Then("It should return all elements in score descending order") {
                    popped.elements.map { it.member } shouldBe listOf("m2", "m1")
                    popped.elements.map { it.score } shouldBe listOf(20, 10)
                }
            }
        }
    }
}
