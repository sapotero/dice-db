package dicedb.core.domain

import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
import java.time.Instant
import kotlinx.coroutines.runBlocking
import test.base.DiceDBSpec

class ExpireTimeCommandSpec : DiceDBSpec() {

    init {
        Given("a client connected to DiceDB") {
            When("using EXPIRETIME after setting a future expiry") {
                Then("it should return correct unix time") {
                    runBlocking {
                        val expireAt = Instant.now().epochSecond + 10

                        client.fire(Command.Set("test_key", "test_value"))
                        client.fire(Command.ExpireAt("test_key", expireAt)).isChanged shouldBe true
                        client.fire(Command.ExpireTime("test_key")).unixSec shouldBe expireAt
                    }
                }
            }

            When("calling EXPIRETIME on a nonexistent key") {
                Then("it should return -2") {
                    runBlocking {
                        client.fire(Command.ExpireTime("non_existent_key")).unixSec shouldBe -2
                    }
                }
            }

            When("calling EXPIRETIME after expired time") {
                Then("it should return -2") {
                    runBlocking {
                        client.fire(Command.Set("k1", "v1"))
                        client.fire(Command.ExpireAt("k1", 100)).isChanged shouldBe true
                        client.fire(Command.ExpireTime("k1")).unixSec shouldBe -2
                    }
                }
            }

            When("calling EXPIRETIME with invalid syntax") {
                Then("it should return error for wrong argument count") {
                    runBlocking {
                        client.fire(Command.Set("test_key", "test_value"))

                        shouldThrowAny { client.fire(Command.Raw("EXPIRETIME")) }

                        shouldThrowAny { client.fire(Command.Raw("EXPIRETIME key1 key2")) }
                    }
                }
            }
        }
    }
}
