package dicedb.core.domain

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import test.base.DiceDBSpec

class DelCommandSpec : DiceDBSpec() {
    init {

        Given("a client connected to DiceDB") {
            When("DEL is called on a key that exists") {
                Then("it should delete it and return 1") {
                    runBlocking {
                        client.fire(Command.Set("k1", "v1"))
                        val del = client.fire(Command.Del("k1"))
                        del.count shouldBe 1

                        val get = client.fire(Command.Get("k1"))
                        get.value shouldBe ""
                    }
                }
            }

            When("DEL is called on multiple existing keys") {
                Then("it should delete both and return 2") {
                    runBlocking {
                        client.fire(Command.Set("k1", "v1"))
                        client.fire(Command.Set("k2", "v2"))

                        val del = client.fire(Command.Del("k1", "k2"))
                        del.count shouldBe 2

                        client.fire(Command.Get("k1")).value shouldBe ""
                        client.fire(Command.Get("k2")).value shouldBe ""
                    }
                }
            }

            When("DEL is called on a non-existent key") {
                Then("it should return 0") {
                    runBlocking {
                        val get = client.fire(Command.Get("k3"))
                        get.value shouldBe ""

                        val del = client.fire(Command.Del("k3"))
                        del.count shouldBe 0
                    }
                }
            }

            When("DEL is called with no arguments") {
                Then("it should return 0 (handled safely)") {
                    runBlocking {
                        val del = client.fire(Command.Del("DEL"))
                        del.count shouldBe 0
                    }
                }
            }
        }
    }
}
