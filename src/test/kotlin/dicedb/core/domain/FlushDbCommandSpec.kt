package dicedb.core.domain

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import test.base.DiceDBSpec

class FlushDbCommandSpec : DiceDBSpec() {

    init {
        Given("a client connected to DiceDB") {
            When("multiple keys are set and FLUSHDB is executed") {
                Then("all keys should be deleted") {
                    runBlocking {
                        client.fire(Command.Set("k1", "v1"))
                        client.fire(Command.Set("k2", "v2"))
                        client.fire(Command.Set("k3", "v3"))

                        client.fire(Command.FlushDb)

                        client.fire(Command.Get("k1")).value shouldBe ""
                        client.fire(Command.Get("k2")).value shouldBe ""
                        client.fire(Command.Get("k3")).value shouldBe ""
                    }
                }
            }
        }
    }
}
