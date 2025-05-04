package dicedb.core.domain

import io.kotest.matchers.shouldBe
import java.time.Instant
import kotlinx.coroutines.delay
import test.base.DiceDBSpec

class SetCommandSpec : DiceDBSpec() {

    init {
        Given("A running DiceDB instance") {
            When("SET and GET simple value") {
                val r1 = client.fire(Command.Set("k", "v"))
                val r2 = client.fire(Command.Get("k"))

                Then("It should return the correct value") { r2.value shouldBe "v" }
            }

            When("SET with EX expires key after delay") {
                client.fire(Command.Set("k100", "v1", Command.ExpireType.EX, expireTime = 2))
                val before = client.fire(Command.Get("k100"))
                delay(3000)
                val after = client.fire(Command.Get("k100"))

                Then("It should expire the key") {
                    before.value shouldBe "v1"
                    after.value shouldBe ""
                }
            }

            When("SET with PX expires key after delay") {
                client.fire(Command.Set("k200", "v2", Command.ExpireType.PX, expireTime = 2000))
                val before = client.fire(Command.Get("k200"))
                delay(3000)
                val after = client.fire(Command.Get("k200"))

                Then("It should expire the key") {
                    before.value shouldBe "v2"
                    after.value shouldBe ""
                }
            }

            When("SET with PXAT sets expiration by timestamp") {
                val timestamp = Instant.now().plusSeconds(2).toEpochMilli()
                client.fire(Command.Set("k", "v", Command.ExpireType.PXAT, expireTime = timestamp))
                val result = client.fire(Command.Get("k"))

                Then("It should exist before expiration") { result.value shouldBe "v" }
            }

            When("SET with NX prevents overwrite") {
                client.fire(Command.Set("nx-key", "v"))
                val res = client.fire(Command.Set("nx-key", "v2", flag = Command.SetFlag.NX))

                Then("It should not overwrite existing value") {
                    client.fire(Command.Get("nx-key")).value shouldBe "v"
                }
            }

            When("SET with XX only updates existing key") {
                client.fire(Command.Set("xx-key", "v"))
                val res = client.fire(Command.Set("xx-key", "v2", flag = Command.SetFlag.XX))

                Then("It should update the existing key") {
                    client.fire(Command.Get("xx-key")).value shouldBe "v2"
                }
            }

            When("SET with KEEPTTL retains original TTL") {
                client.fire(Command.Set("ttl-key", "v", Command.ExpireType.EX, expireTime = 2))
                delay(1000)
                client.fire(Command.Set("ttl-key", "v2", keepTtl = true))
                delay(1500)
                val res = client.fire(Command.Get("ttl-key"))

                Then("It should be expired due to preserved TTL") { res.value shouldBe "" }
            }

            When("SET with invalid arguments throws error") {
                val result = runCatching { client.fire(Command.Raw("SET")) }

                Then("It should fail") { result.isFailure shouldBe true }
            }
        }
    }
}
