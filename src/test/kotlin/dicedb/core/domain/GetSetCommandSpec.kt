package dicedb.core.domain

import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import test.base.DiceDBSpec

class GetSetCommandSpec : DiceDBSpec() {
    init {
        Given("GETSET command") {
            Then("SET with expiration and GETSET") {
                client.fire(
                    Command.Set("k", "v", expireType = Command.ExpireType.EX, expireTime = 22)
                )
                client.fire(Command.GetSet("k", "v2")).value shouldBe "v"
                delay(2.seconds)
                client.fire(Command.Ttl("k")).seconds shouldBe -1
            }

            Then("GETSET without expiration") {
                client.fire(Command.Set("k1", "v"))
                client.fire(Command.Get("k1")).value shouldBe "v"
                client.fire(Command.GetSet("k1", "v2")).value shouldBe "v"
                client.fire(Command.Get("k1")).value shouldBe "v2"
            }

            Then("GETSET with non-existent key") {
                client.fire(Command.GetSet("nek", "v")).value shouldBe ""
                client.fire(Command.Get("nek")).value shouldBe "v"
            }
        }
    }
}
