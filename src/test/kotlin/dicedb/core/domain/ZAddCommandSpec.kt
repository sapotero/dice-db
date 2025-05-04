package dicedb.core.domain

import io.kotest.matchers.shouldBe
import test.base.DiceDBSpec

class ZAddCommandSpec : DiceDBSpec() {
    init {
        Given("A running DiceDB instance") {
            When("ZADD is called with no arguments") {
                val result = runCatching { client.fire(Command.Raw("ZADD")) }

                Then("It should return argument error") { result.isFailure shouldBe true }
            }

            When("ZADD is called with key only") {
                val result = runCatching { client.fire(Command.Raw("ZADD key")) }

                Then("It should return argument error") { result.isFailure shouldBe true }
            }

            When("ZADD is called with key and score only") {
                val result = runCatching { client.fire(Command.Raw("ZADD key 1")) }

                Then("It should return argument error") { result.isFailure shouldBe true }
            }

            When("ZADD is used with NX, XX, CH flags") {
                val added =
                    client.fire(
                        Command.ZAdd("key", listOf(1 to "memberNX"), listOf(Command.AddFlag.NX))
                    )
                val notUpdated =
                    client.fire(
                        Command.ZAdd("key", listOf(2 to "memberNX"), listOf(Command.AddFlag.NX))
                    )
                val updated =
                    client.fire(
                        Command.ZAdd("key", listOf(3 to "memberNX"), listOf(Command.AddFlag.XX))
                    )
                val notAdded =
                    client.fire(
                        Command.ZAdd("key", listOf(1 to "memberXX"), listOf(Command.AddFlag.XX))
                    )
                val changed =
                    client.fire(
                        Command.ZAdd("key", listOf(4 to "memberNX"), listOf(Command.AddFlag.CH))
                    )

                Then("It should handle flags correctly") {
                    added.count shouldBe 1
                    notUpdated.count shouldBe 0
                    updated.count shouldBe 0
                    notAdded.count shouldBe 0
                    changed.count shouldBe 1
                }
            }

            When("ZADD is used with GT and LT flags") {
                client.fire(Command.ZAdd("key", listOf(2 to "memberGT")))
                val updateGT =
                    client.fire(
                        Command.ZAdd("key", listOf(3 to "memberGT"), listOf(Command.AddFlag.GT))
                    )
                val noUpdateGT =
                    client.fire(
                        Command.ZAdd("key", listOf(1 to "memberGT"), listOf(Command.AddFlag.GT))
                    )
                client.fire(Command.ZAdd("key", listOf(1 to "memberLT")))
                val updateLT =
                    client.fire(
                        Command.ZAdd("key", listOf(0 to "memberLT"), listOf(Command.AddFlag.LT))
                    )
                val noUpdateLT =
                    client.fire(
                        Command.ZAdd("key", listOf(2 to "memberLT"), listOf(Command.AddFlag.LT))
                    )

                Then("It should respect GT and LT conditions") {
                    updateGT.count shouldBe 0
                    noUpdateGT.count shouldBe 0
                    updateLT.count shouldBe 0
                    noUpdateLT.count shouldBe 0
                }
            }

            When("ZADD is used with invalid flag combinations") {
                val r1 = runCatching { client.fire(Command.Raw("ZADD key NX XX 1 member")) }
                val r2 = runCatching { client.fire(Command.Raw("ZADD key GT LT 1 member")) }
                val r3 = runCatching { client.fire(Command.Raw("ZADD key INCR 1 m1 2 m2")) }

                Then("It should return compatibility errors") {
                    r1.isFailure shouldBe true
                    r2.isFailure shouldBe true
                    r3.isFailure shouldBe true
                }
            }

            When("ZADD is used with CH and multiple inserts") {
                val r1 =
                    client.fire(
                        Command.ZAdd("myzset", listOf(1 to "a"), listOf(Command.AddFlag.CH))
                    )
                val r2 =
                    client.fire(
                        Command.ZAdd("myzset", listOf(2 to "a"), listOf(Command.AddFlag.CH))
                    )
                val r3 =
                    client.fire(
                        Command.ZAdd("myzset", listOf(3 to "b"), listOf(Command.AddFlag.CH))
                    )

                Then("It should return 1 change each time") {
                    r1.count shouldBe 1
                    r2.count shouldBe 1
                    r3.count shouldBe 1
                }
            }
        }
    }
}
