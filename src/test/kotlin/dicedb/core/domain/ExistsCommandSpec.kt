package dicedb.core.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import test.base.DiceDBSpec

class ExistsCommandSpec : DiceDBSpec() {
    init {

        Given("a client connected to DiceDB") {
            When("checking EXISTS for an existing key") {
                Then("it should return 1, and 0 for non-existent key") {
                    runBlocking {
                        client.fire(Command.Set("key", "value"))
                        client.fire(Command.Exists("key")).count shouldBe 1
                        client.fire(Command.Exists("key2")).count shouldBe 0
                    }
                }
            }

            When("checking EXISTS for multiple keys") {
                Then("it should count existing keys") {
                    runBlocking {
                        client.fire(Command.Set("key", "value"))
                        client.fire(Command.Set("key2", "value2"))

                        client.fire(Command.Exists("key", "key2", "key3")).count shouldBe 2
                        client.fire(Command.Exists("key", "key2", "key3", "key4")).count shouldBe 2

                        client.fire(Command.Del("key"))
                        client.fire(Command.Exists("key", "key2", "key3", "key4")).count shouldBe 1
                    }
                }
            }

            When("checking EXISTS after key expiration") {
                Then("it should return 1 then 0") {
                    runBlocking {
                        client.fire(Command.Set("key", "value", Command.ExpireType.EX, 1))
                        client.fire(Command.Exists("key")).count shouldBe 1
                        delay(1001)
                        client.fire(Command.Exists("key")).count shouldBe 0
                    }
                }
            }

            When("checking EXISTS with mixed expired and active keys") {
                Then("it should return 3 then 2") {
                    runBlocking {
                        client.fire(Command.Set("key", "value", Command.ExpireType.EX, 2))
                        client.fire(Command.Set("key2", "value2"))
                        client.fire(Command.Set("key3", "value3"))

                        client.fire(Command.Exists("key", "key2", "key3")).count shouldBe 3
                        delay(2000)
                        client.fire(Command.Exists("key", "key2", "key3")).count shouldBe 2
                    }
                }
            }

            When("checking EXISTS with no arguments") {
                Then("should throw an exception") {
                    runBlocking {
                        shouldThrow<IllegalStateException> { client.fire(Command.Exists()) }
                    }
                }
            }

            When("checking EXISTS with duplicate keys") {
                Then("it should count them all") {
                    runBlocking {
                        client.fire(Command.Set("key", "value"))
                        client.fire(Command.Exists("key", "key")).count shouldBe 2
                    }
                }
            }

            When("checking EXISTS with duplicate nonexistent keys") {
                Then("it should count only existing ones") {
                    runBlocking {
                        client.fire(Command.Set("key", "value"))
                        client.fire(Command.Exists("key", "neq", "neq2")).count shouldBe 1
                    }
                }
            }

            When("checking EXISTS with mix of duplicates and multiple keys") {
                Then("it should count correctly") {
                    runBlocking {
                        client.fire(Command.Set("key", "value"))
                        client.fire(Command.Set("key1", "value"))
                        client.fire(Command.Exists("key", "key", "key1")).count shouldBe 3
                    }
                }
            }
        }
    }
}
