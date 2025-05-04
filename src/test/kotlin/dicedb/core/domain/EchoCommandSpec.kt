package dicedb.core.domain

import dicedb.core.proto.Response
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import test.base.DiceDBSpec

class EchoCommandSpec : DiceDBSpec() {
    init {

        Given("a DiceDB container and connected client") {
            When("sending ECHO with one argument") {
                Then("it should return the same message") {
                    runBlocking {
                        val response = client.fire<Response.ECHORes>(Command.Echo("hello!"))
                        response.message shouldBe "hello!"
                    }
                }
            }

            When("sending ECHO with no arguments") {
                Then("it should return an error") {
                    runBlocking {
                        val exception =
                            shouldThrow<IllegalStateException> {
                                client.fire<Response.ECHORes>(Command.Raw("ECHO"))
                            }
                        exception.message shouldBe
                            "Command failed: wrong number of arguments for 'ECHO' command"
                    }
                }
            }
        }
    }
}
