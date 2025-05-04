package dicedb.core.domain

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import test.base.DiceDBSpec

class DecrCommandSpec : DiceDBSpec() {
    init {
        Given("a client connected to DiceDB") {
            val key = "key1"

            When("setting `$key` to 2 and decrementing multiple times") {
                client.fire(Command.Set(key, "2"))

                Then("values should follow 2 -> 1 -> 0 -> -1") {
                    runBlocking {
                        val getResult = client.fire(Command.Get(key))
                        getResult.value shouldBe "2"

                        // First DECR -> 1
                        val res1 = client.fire(Command.Decr(key))
                        res1.value shouldBe 1L

                        // Second DECR -> 0
                        val res2 = client.fire(Command.Decr(key))
                        res2.value shouldBe 0

                        // Third DECR -> -1
                        val res3 = client.fire(Command.Decr(key))
                        res3.value shouldBe -1L
                    }
                }
            }
        }
    }
}
