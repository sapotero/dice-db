package dicedb.core.domain

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import test.base.DiceDBSpec

class TtlCommandSpec : DiceDBSpec() {

    init {
        Given("A running DiceDB instance") {
            When("TTL Simple Value") {
                client.fire(Command.Set("k101", "v1"))
                client.fire(Command.GetEx("k101", Command.ExpireType.EX, time = 5))
                delay(500)
                val ttl = client.fire(Command.Ttl("k101"))

                Then("TTL should be around 4 seconds") { ttl.seconds shouldBe 4 }
            }

            When("TTL on Non-Existent Key") {
                val ttl = client.fire(Command.Ttl("foo1"))

                Then("It should return -2") { ttl.seconds shouldBe -2 }
            }

            When("TTL with negative expiry") {
                client.fire(Command.Set("foo", "bar"))
                val result = runCatching {
                    client.fire(Command.GetEx("foo", Command.ExpireType.EX, time = -5))
                }

                Then("It should return an error") { result.isFailure shouldBe true }
            }

            When("TTL without Expiry") {
                client.fire(Command.Set("foo2", "bar"))
                client.fire(Command.Get("foo2"))
                val ttl = client.fire(Command.Ttl("foo2"))

                Then("TTL should be -1 (no expiry)") { ttl.seconds shouldBe -1 }
            }

            When("TTL after DEL") {
                client.fire(Command.Set("foo", "bar"))
                client.fire(Command.GetEx("foo", Command.ExpireType.EX, time = 5))
                delay(1000)
                client.fire(Command.Del("foo"))
                val ttl = client.fire(Command.Ttl("foo"))

                Then("TTL should be -2 (deleted)") { ttl.seconds shouldBe -2 }
            }

            When("Multiple TTL updates") {
                client.fire(Command.Set("foo", "bar"))
                client.fire(Command.GetEx("foo", Command.ExpireType.EX, time = 10))
                client.fire(Command.GetEx("foo", Command.ExpireType.EX, time = 5))
                delay(500)
                val ttl = client.fire(Command.Ttl("foo"))

                Then("TTL should be around 4 seconds") { ttl.seconds shouldBe 4 }
            }

            When("TTL with Persist") {
                client.fire(Command.Set("foo3", "bar"))
                client.fire(Command.GetEx("foo3", persist = true))
                val ttl = client.fire(Command.Ttl("foo3"))

                Then("TTL should be -1") { ttl.seconds shouldBe -1 }
            }

            When("TTL with Expire and Expired Key") {
                client.fire(Command.Set("foo", "bar"))
                client.fire(Command.GetEx("foo", Command.ExpireType.EX, time = 2))
                delay(500)
                val ttl = client.fire(Command.Ttl("foo"))
                delay(5000)
                val get = client.fire(Command.Get("foo"))

                Then("TTL should be 1, then key should expire") {
                    ttl.seconds shouldBe 1
                    get.value shouldBe ""
                }
            }
        }
    }
}
