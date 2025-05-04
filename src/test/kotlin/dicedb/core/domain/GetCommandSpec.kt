package dicedb.core.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import test.base.DiceDBSpec

class GetCommandSpec : DiceDBSpec() {

    init {
        Given("a client connected to DiceDB") {
            When("setting a key with expiration and getting it twice") {
                Then("it should exist, then expire") {
                    runBlocking {
                        client.fire(Command.Set("k978", "v", Command.ExpireType.EX, 2))
                        client.fire(Command.Get("k978")).value shouldBe "v"
                        delay(5000)
                        client.fire(Command.Get("k978")).value shouldBe ""
                    }
                }
            }

            When("setting a key without expiration") {
                Then("GET should return the value") {
                    runBlocking {
                        client.fire(Command.Set("k", "v"))
                        client.fire(Command.Get("k")).value shouldBe "v"
                    }
                }
            }

            When("setting a floating point value") {
                Then("GET should return the value as stringified float") {
                    runBlocking {
                        client.fire(Command.Set("fp", "123.123"))
                        client.fire(Command.Get("fp")).value shouldBe "123.123000"
                    }
                }
            }

            When("calling GET on a hash") {
                Then("should return object type error") {
                    client.fire(Command.HSet("map", listOf("k1", "v1")))
                    shouldThrow<IllegalStateException> { client.fire(Command.Get("map")) }
                }
            }

            When("getting a non-existent key") {
                Then("should return an empty string") {
                    runBlocking { client.fire(Command.Get("nek")).value shouldBe "" }
                }
            }

            When("GET with no arguments") {
                Then("should return argument count error") {
                    runBlocking {
                        val ex = runCatching { client.fire(Command.Raw("GET")) }.exceptionOrNull()
                        ex?.message shouldBe
                            "Command failed: wrong number of arguments for 'GET' command"
                    }
                }
            }
        }
    }
}
