package dicedb.core.domain

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import test.base.DiceDBSpec

class DecrByCommandSpec : DiceDBSpec() {
    init {
        Given("a client connected to DiceDB") {
            val key = "key1"

            When("setting $key to 5 and decrementing by steps") {
                client.fire(Command.Set(key, "5"))

                Then("values should follow 5 -> 3 -> 1 -> 0 -> -1") {
                    runBlocking {
                        val getResult = client.fire(Command.Get(key))
                        getResult.value shouldBe "5"

                        // DECRBY key1 2 -> 3
                        val res1 = client.fire(Command.DecrBy(key, 2))
                        res1.value shouldBe 3

                        // DECRBY key1 2 -> 1
                        val res2 = client.fire(Command.DecrBy(key, 2))
                        res2.value shouldBe 1

                        // DECRBY key1 1 -> 0
                        val res3 = client.fire(Command.DecrBy(key, 1))
                        res3.value shouldBe 0

                        // DECRBY key1 1 -> -1
                        val res4 = client.fire(Command.DecrBy(key, 1))
                        res4.value shouldBe -1
                    }
                }
            }
        }
    }
}
