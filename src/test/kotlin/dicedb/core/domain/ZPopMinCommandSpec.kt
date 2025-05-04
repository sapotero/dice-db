package dicedb.core.domain

import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
import test.base.DiceDBSpec

class ZPopMinCommandSpec : DiceDBSpec() {
    init {
        Given("A sorted set with various members") {
            When("ZPOPMIN is called on a non-existent key") {
                val result = client.fire(Command.ZPopMin("nonexistent"))
                Then("It should return an empty list") { result.elements shouldBe emptyList() }
            }

            When("ZPOPMIN is called on a string key") {
                client.fire(Command.Set("strkey", "value"))
                Then("It should fail with a type error") {
                    shouldThrowAny { client.fire(Command.ZPopMin("strkey")) }
                }
            }

            When("ZPOPMIN is called without count") {
                client.fire(Command.ZAdd("myzset", listOf(1 to "a", 2 to "b", 3 to "c")))
                val popped = client.fire(Command.ZPopMin("myzset"))
                Then("It should return the element with the lowest score") {
                    popped.elements.map { it.member } shouldBe listOf("a")
                    popped.elements.map { it.score } shouldBe listOf(1)
                }
            }

            When("ZPOPMIN is called with count = 2") {
                client.fire(Command.ZAdd("myzset2", listOf(1 to "x", 2 to "y", 3 to "z")))
                val popped = client.fire(Command.ZPopMin("myzset2", count = 2))
                Then("It should return two lowest scored elements") {
                    popped.elements.map { it.member } shouldBe listOf("x", "y")
                    popped.elements.map { it.score } shouldBe listOf(1, 2)
                }
            }

            When("ZPOPMIN is called with count > size") {
                client.fire(Command.ZAdd("myzset3", listOf(10 to "m1", 20 to "m2")))
                val popped = client.fire(Command.ZPopMin("myzset3", count = 10))
                Then("It should return all members in ascending score order") {
                    popped.elements.map { it.member } shouldBe listOf("m1", "m2")
                    popped.elements.map { it.score } shouldBe listOf(10, 20)
                }
            }

            When("ZPOPMIN is called repeatedly on empty set") {
                client.fire(Command.ZAdd("myzset4", listOf(99 to "last")))
                client.fire(Command.ZPopMin("myzset4"))
                val res = client.fire(Command.ZPopMin("myzset4"))
                Then("It should return empty list") { res.elements shouldBe emptyList() }
            }
        }
    }
}
