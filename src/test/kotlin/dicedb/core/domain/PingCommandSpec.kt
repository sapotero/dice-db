package dicedb.core.domain

import dicedb.core.proto.Response
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import test.base.DiceDBSpec

class PingCommandSpec : DiceDBSpec() {
    init {
        Given("a connected DiceDB client") {
            When("sending PING with no arguments") {
                Then("it should return 'PONG'") {
                    runBlocking {
                        val result = client.fire<Response.PINGRes>(Command.Ping())
                        result.message shouldBe "PONG"
                    }
                }
            }

            When("sending PING with one argument") {
                Then("it should return 'PONG <arg>'") {
                    runBlocking {
                        val result = client.fire<Response.PINGRes>(Command.Ping("hello"))
                        result.message shouldBe "PONG hello"
                    }
                }
            }
        }
    }
}
