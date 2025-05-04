package dicedb.core.domain

import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
import test.base.DiceDBSpec

class IncrByCommandSpec : DiceDBSpec() {

    init {
        Given("A fresh DiceDB instance") {
            When("INCRBY is called on an integer key") {
                val set = client.fire(Command.Set("key", "3"))
                val incr1 = client.fire(Command.IncrBy("key", 2))
                val incr2 = client.fire(Command.IncrBy("key", 1))
                val get = client.fire(Command.Get("key"))

                Then("It should return incremented values") {
                    incr1.value shouldBe 5
                    incr2.value shouldBe 6
                    get.value shouldBe "6"
                }
            }

            When("INCRBY is called with negative values") {
                val set = client.fire(Command.Set("key", "100"))
                val r1 = client.fire(Command.IncrBy("key", -2))
                val r2 = client.fire(Command.IncrBy("key", -10))
                val r3 = client.fire(Command.IncrBy("key", -88))
                val r4 = client.fire(Command.IncrBy("key", -100))
                val get = client.fire(Command.Get("key"))

                Then("It should decrement correctly") {
                    r1.value shouldBe 98
                    r2.value shouldBe 88
                    r3.value shouldBe 0
                    r4.value shouldBe -100
                    get.value shouldBe "-100"
                }
            }

            When("INCRBY is called on a non-existent key") {
                val set = client.fire(Command.Set("key", "3"))
                val r1 = client.fire(Command.IncrBy("unsetKey", 2))
                val g1 = client.fire(Command.Get("key"))
                val g2 = client.fire(Command.Get("unsetKey"))

                Then("It should create unsetKey and leave key unchanged") {
                    r1.value shouldBe 2
                    g1.value shouldBe "3"
                    g2.value shouldBe "2"
                }
            }

            When("INCRBY causes int64 rollover from MAX to MIN") {
                val max = Long.MAX_VALUE
                val set = client.fire(Command.Set("key", "${max - 1}"))
                val r1 = client.fire(Command.IncrBy("key", 1))
                val r2 = client.fire(Command.IncrBy("key", 1))
                val get = client.fire(Command.Get("key"))

                Then("It should wrap to MIN") {
                    r1.value shouldBe max
                    r2.value shouldBe Long.MIN_VALUE
                    get.value shouldBe "${Long.MIN_VALUE}"
                }
            }

            When("INCRBY causes int64 rollover from MIN to MAX") {
                val minPlusOne = Long.MIN_VALUE + 1
                val set = client.fire(Command.Set("key", "$minPlusOne"))
                val r1 = client.fire(Command.IncrBy("key", -1))
                val r2 = client.fire(Command.IncrBy("key", -1))
                val get = client.fire(Command.Get("key"))

                Then("It should wrap to MAX") {
                    r1.value shouldBe Long.MIN_VALUE
                    r2.value shouldBe Long.MAX_VALUE
                    get.value shouldBe "${Long.MAX_VALUE}"
                }
            }

            When("INCRBY is called on a string value") {
                val set = client.fire(Command.Set("key", "1"))

                Then("It should throw a type error") {
                    shouldThrowAny { client.fire(Command.Raw("INCRBY key abc")) }
                }
            }
        }
    }
}
