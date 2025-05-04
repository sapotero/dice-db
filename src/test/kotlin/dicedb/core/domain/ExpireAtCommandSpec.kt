package dicedb.core.domain

import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
import java.time.Instant
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import test.base.DiceDBSpec

class ExpireAtCommandSpec : DiceDBSpec() {

    init {
        Given("a client connected to DiceDB") {
            When("calling EXPIREAT on an existing key") {
                Then("it should return true") {
                    runBlocking {
                        val unixTime = Instant.now().epochSecond + 1
                        client.fire(Command.Set("test_key", "test_value"))
                        client.fire(Command.ExpireAt("test_key", unixTime)).isChanged shouldBe true
                    }
                }
            }

            When("EXPIREAT then GET after expiration") {
                Then("value should be empty") {
                    runBlocking {
                        val unixTime = Instant.now().epochSecond + 1
                        client.fire(Command.Set("k1", "v1"))
                        client.fire(Command.ExpireAt("k1", unixTime)).isChanged shouldBe true
                        delay(1000)
                        client.fire(Command.Get("k1")).value shouldBe ""
                    }
                }
            }

            When("EXPIREAT on non-existent key") {
                Then("it should return false") {
                    runBlocking {
                        val unixTime = Instant.now().epochSecond + 1
                        client
                            .fire(Command.ExpireAt("non_existent_key", unixTime))
                            .isChanged shouldBe false
                    }
                }
            }

            When("EXPIREAT with past time") {
                Then("it should expire immediately") {
                    runBlocking {
                        client.fire(Command.Set("k3", "v3"))
                        client.fire(Command.ExpireAt("k3", 20)).isChanged shouldBe true
                        delay(1000)
                        client.fire(Command.Get("k3")).value shouldBe ""
                    }
                }
            }

            When("EXPIREAT with NX flag") {
                Then("should succeed once, then fail") {
                    runBlocking {
                        val time = Instant.now().epochSecond + 10
                        client.fire(Command.Set("test_key", "test_value"))
                        client
                            .fire(Command.ExpireAt("test_key", time, Command.SetFlag.NX))
                            .isChanged shouldBe true
                        client
                            .fire(Command.ExpireAt("test_key", time - 9, Command.SetFlag.NX))
                            .isChanged shouldBe false
                    }
                }
            }

            When("EXPIREAT with XX flag") {
                Then("should fail first, succeed later") {
                    runBlocking {
                        val time = Instant.now().epochSecond + 10
                        client.fire(Command.Set("test_key", "test_value"))
                        client
                            .fire(Command.ExpireAt("test_key", time, Command.SetFlag.XX))
                            .isChanged shouldBe false
                        client.fire(Command.ExpireAt("test_key", time)).isChanged shouldBe true
                        client
                            .fire(Command.ExpireAt("test_key", time, Command.SetFlag.XX))
                            .isChanged shouldBe true
                    }
                }
            }

            When("GET value after EXPIREAT NX + delay") {
                Then("it should be empty") {
                    runBlocking {
                        val time = Instant.now().epochSecond + 2
                        client.fire(Command.Set("k2", "v2"))
                        client
                            .fire(Command.ExpireAt("k2", time, Command.SetFlag.NX))
                            .isChanged shouldBe true
                        delay(2000)
                        client.fire(Command.Get("k2")).value shouldBe ""
                    }
                }
            }

            When("calling EXPIREAT with invalid combinations of flags") {
                Then("each case should return a validation error") {
                    runBlocking {
                        val baseTime = Instant.now().epochSecond + 1
                        client.fire(Command.Set("test_key", "test_value"))

                        val badCommands =
                            listOf(
                                "EXPIREAT test_key $baseTime rr",
                                "EXPIREAT test_key $baseTime XX NX",
                                "EXPIREAT test_key $baseTime GT LT",
                                "EXPIREAT test_key $baseTime GT LT XX",
                                "EXPIREAT test_key $baseTime GT LT NX",
                                "EXPIREAT test_key $baseTime NX XX GT",
                                "EXPIREAT test_key $baseTime NX XX LT",
                            )

                        badCommands.forEach { cmd ->
                            shouldThrowAny { client.fire(Command.Raw(cmd)) }
                        }
                    }
                }
            }
        }
    }
}
