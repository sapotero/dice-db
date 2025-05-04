package dicedb.core.domain

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import test.base.DiceDBSpec

class GetDelCommandSpec : DiceDBSpec() {

    init {
        Given("a client connected to DiceDB") {
            When("GETDEL is called on an existing key") {
                Then("it should return the value and delete the key") {
                    runBlocking {
                        client.fire(Command.Set("k", "v"))
                        client.fire(Command.GetDel("k")).value shouldBe "v"
                        client.fire(Command.GetDel("k")).value shouldBe null
                        client.fire(Command.Get("k")).value shouldBe ""
                    }
                }
            }

            When("GETDEL is called on expired key") {
                Then("it should return null") {
                    runBlocking {
                        client.fire(Command.GetDel("k")).value shouldBe null
                        client.fire(Command.Set("k", "v", Command.ExpireType.EX, 2))
                        delay(2500)
                        client.fire(Command.GetDel("k")).value shouldBe null
                    }
                }
            }

            When("GETDEL is called before key expires") {
                Then("it should return the value and delete it") {
                    runBlocking {
                        client.fire(Command.Set("k", "v", Command.ExpireType.EX, 40))
                        delay(2000)
                        client.fire(Command.GetDel("k")).value shouldBe "v"
                    }
                }
            }
        }
    }
}
