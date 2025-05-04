package dicedb.core.domain

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import test.base.DiceDBSpec

class ExpireCommandSpec : DiceDBSpec() {

    init {
        Given("a client connected to DiceDB") {
            When("calling EXPIRE on a valid key") {
                Then("it should return true") {
                    runBlocking {
                        client.fire(Command.Set("test_key", "test_value"))
                        client.fire(Command.Expire("test_key", 1)).isChanged shouldBe true
                    }
                }
            }

            When("EXPIRE key then GET after expiration") {
                Then("key should be nil") {
                    runBlocking {
                        client.fire(Command.Set("k1", "v1"))
                        client.fire(Command.Expire("k1", 1)).isChanged shouldBe true
                        delay(1000)
                        client.fire(Command.Get("k1")).value shouldBe ""
                    }
                }
            }

            When("calling EXPIRE on a nonexistent key") {
                Then("it should return false") {
                    runBlocking {
                        client.fire(Command.Expire("non_existent_key", 1)).isChanged shouldBe false
                    }
                }
            }

            When("calling EXPIRE with past time") {
                Then("it should fail with an error") {
                    runBlocking {
                        val ex =
                            kotlin
                                .runCatching { client.fire(Command.Expire("test_key", -1)) }
                                .exceptionOrNull()
                        ex?.message shouldBe
                            "Command failed: invalid expire time in 'EXPIRE' command"
                    }
                }
            }

            When("EXPIRE with NX flag (set only if no TTL)") {
                Then("first EXPIRE returns true, second false") {
                    runBlocking {
                        client.fire(Command.Set("test_key", "test_value"))
                        client
                            .fire(Command.Expire("test_key", 1, Command.SetFlag.NX))
                            .isChanged shouldBe true
                        client
                            .fire(Command.Expire("test_key", 1, Command.SetFlag.NX))
                            .isChanged shouldBe false
                    }
                }
            }

            When("EXPIRE with XX flag (set only if already has TTL)") {
                Then("first returns false, second returns true twice") {
                    runBlocking {
                        client.fire(Command.Set("test_key", "test_value"))
                        client
                            .fire(Command.Expire("test_key", 10, Command.SetFlag.XX))
                            .isChanged shouldBe false
                        client.fire(Command.Expire("test_key", 10)).isChanged shouldBe true
                        client
                            .fire(Command.Expire("test_key", 10, Command.SetFlag.XX))
                            .isChanged shouldBe true
                    }
                }
            }

            When("GET value after expiration (standard)") {
                Then("it should be nil") {
                    runBlocking {
                        client.fire(Command.Set("test_key", "test_value"))
                        client.fire(Command.Expire("test_key", 2)).isChanged shouldBe true
                        delay(2000)
                        client.fire(Command.Get("test_key")).value shouldBe ""
                    }
                }
            }

            When("GET value after expiration using NX") {
                Then("it should be nil") {
                    runBlocking {
                        client.fire(Command.Set("test_key", "test_value"))
                        client
                            .fire(Command.Expire("test_key", 2, Command.SetFlag.NX))
                            .isChanged shouldBe true
                        delay(2000)
                        client.fire(Command.Get("test_key")).value shouldBe ""
                    }
                }
            }

            When("calling EXPIRE with no arguments") {
                Then("it should return error") {
                    runBlocking {
                        val ex =
                            kotlin
                                .runCatching { client.fire(Command.Raw("EXPIRE")) }
                                .exceptionOrNull()
                        ex?.message shouldBe
                            "Command failed: wrong number of arguments for 'EXPIRE' command"
                    }
                }
            }
        }
    }
}
