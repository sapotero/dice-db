package dicedb.core.domain

import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
import test.base.DiceDBSpec

class IncrCommandSpec : DiceDBSpec() {

    init {
        Given("An active DiceDB client") {
            When("INCR is used on an integer key") {
                Then("It should increment the value") {
                    client.fire(Command.Set("key1", "0"))

                    client.fire(Command.Incr("key1")).value shouldBe 1
                    client.fire(Command.Incr("key1")).value shouldBe 2
                    client.fire(Command.Incr("key2")).value shouldBe 1
                    client.fire(Command.Get("key1")).value shouldBe "2"
                    client.fire(Command.Get("key2")).value shouldBe "1"
                }
            }

            When("INCR is used on max and min int64 boundaries") {
                Then("It should handle rollover correctly") {
                    client.fire(Command.Set("max_int", "${Long.MAX_VALUE - 1}"))
                    client.fire(Command.Incr("max_int")).value shouldBe Long.MAX_VALUE
                    client.fire(Command.Incr("max_int")).value shouldBe Long.MIN_VALUE
                    client.fire(Command.Set("max_int", "${Long.MAX_VALUE}"))
                    client.fire(Command.Incr("max_int")).value shouldBe Long.MIN_VALUE

                    client.fire(Command.Set("min_int", "${Long.MIN_VALUE}"))
                    client.fire(Command.Incr("min_int")).value shouldBe Long.MIN_VALUE + 1
                    client.fire(Command.Incr("min_int")).value shouldBe Long.MIN_VALUE + 2
                }
            }

            When("INCR is used on non-integer types") {
                Then("It should return a type error") {
                    client.fire(Command.Set("float_key", "3.14"))
                    shouldThrowAny { client.fire(Command.Incr("float_key")) }
                    client.fire(Command.Set("string_key", "hello"))
                    shouldThrowAny { client.fire(Command.Incr("string_key")) }
                }
            }

            When("INCR is used on a non-existent key") {
                Then("It should create the key and initialize it to 1") {
                    client.fire(Command.Incr("new_key")).value shouldBe 1
                    client.fire(Command.Get("new_key")).value shouldBe "1"
                    client.fire(Command.Incr("new_key")).value shouldBe 2
                }
            }

            When("INCR is used on keys with stringified integers") {
                Then("It should increment correctly") {
                    client.fire(Command.Set("str_int1", "42"))
                    client.fire(Command.Incr("str_int1")).value shouldBe 43
                    client.fire(Command.Set("str_int2", "-10"))
                    client.fire(Command.Incr("str_int2")).value shouldBe -9
                    client.fire(Command.Set("str_int3", "0"))
                    client.fire(Command.Incr("str_int3")).value shouldBe 1
                }
            }
        }
    }
}
