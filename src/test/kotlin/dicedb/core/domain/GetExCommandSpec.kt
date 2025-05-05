package dicedb.core.domain

import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.longs.shouldBeInRange
import io.kotest.matchers.shouldBe
import java.time.Instant
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import test.base.DiceDBSpec

class GetExCommandSpec : DiceDBSpec() {

    init {
        Given("a connected DiceDB client") {
            When("GETEX is used with a simple key") {
                Then("it returns value and leaves key intact") {
                    runBlocking {
                        client.fire(Command.Set("foo", "bar"))
                        client.fire(Command.GetEx("foo")).value shouldBe "bar"
                        client.fire(Command.GetEx("foo")).value shouldBe "bar"
                        client.fire(Command.Ttl("foo")).seconds shouldBe -1
                    }
                }
            }

            When("GETEX is used with non-existent key") {
                Then("it returns empty and TTL -2") {
                    runBlocking {
                        client.fire(Command.GetEx("nonExecFoo")).value shouldBe null
                        client.fire(Command.Ttl("nonExecFoo")).seconds shouldBe -2
                    }
                }
            }

            When("GETEX is used with EX option") {
                Then("it sets expiration in seconds") {
                    runBlocking {
                        client.fire(Command.Set("foo", "bar"))
                        client
                            .fire(
                                Command.GetEx("foo", expireType = Command.ExpireType.EX, time = 2)
                            )
                            .value shouldBe "bar"
                        client.fire(Command.Ttl("foo")).seconds shouldBeInRange 1..2L
                        client.fire(Command.Get("foo")).value shouldBe "bar"
                        delay(2000)
                        client.fire(Command.Get("foo")).value shouldBe ""
                    }
                }
            }

            When("GETEX is used with PX option") {
                Then("it sets expiration in milliseconds") {
                    runBlocking {
                        client.fire(Command.Set("foo", "bar"))
                        client
                            .fire(
                                Command.GetEx(
                                    "foo",
                                    expireType = Command.ExpireType.PX,
                                    time = 2000,
                                )
                            )
                            .value shouldBe "bar"
                        delay(500)
                        client.fire(Command.Ttl("foo")).seconds shouldBe 1
                        client.fire(Command.Get("foo")).value shouldBe "bar"
                        delay(1500)
                        client.fire(Command.Get("foo")).value shouldBe ""
                    }
                }
            }

            When("GETEX is used with PERSIST option") {
                Then("it removes expiration") {
                    runBlocking {
                        client.fire(
                            Command.Set(
                                "foo",
                                "bar",
                                expireType = Command.ExpireType.PX,
                                expireTime = 100,
                            )
                        )
                        client.fire(Command.GetEx("foo", persist = true)).value shouldBe "bar"
                        client.fire(Command.Ttl("foo")).seconds shouldBe -1
                    }
                }
            }

            When("GETEX is used with invalid expiry values") {
                Then("it returns error") {
                    runBlocking {
                        client.fire(Command.Set("foo", "bar"))

                        shouldThrowAny {
                            client.fire(
                                Command.GetEx("foo", expireType = Command.ExpireType.EX, time = -1)
                            )
                        }

                        shouldThrowAny {
                            client.fire(
                                Command.GetEx("foo", expireType = Command.ExpireType.PX, time = 0)
                            )
                        }

                        shouldThrowAny { client.fire(Command.Raw("GETEX foo EX abc")) }
                    }
                }
            }

            When("GETEX is used with PXAT option") {
                Then("it sets expiration to absolute timestamp") {
                    runBlocking {
                        val pxat = Instant.now().plusSeconds(86400).toEpochMilli()
                        client.fire(Command.Set("foo", "bar"))
                        client
                            .fire(
                                Command.GetEx(
                                    "foo",
                                    expireType = Command.ExpireType.PXAT,
                                    time = pxat,
                                )
                            )
                            .value shouldBe "bar"
                    }
                }
            }

            When("GETEX is used with EXAT in the past") {
                Then("it returns error") {
                    runBlocking {
                        val exat = Instant.now().minusSeconds(3600).epochSecond
                        client.fire(Command.Set("foo", "bar"))
                        val err =
                            runCatching {
                                    client.fire(
                                        Command.GetEx(
                                            "foo",
                                            expireType = Command.ExpireType.EXAT,
                                            time = exat,
                                        )
                                    )
                                }
                                .exceptionOrNull()
                        err?.message shouldBe
                            "Command failed: invalid value for a parameter in 'GETEX' command for EXAT parameter"
                    }
                }
            }
        }
    }
}
