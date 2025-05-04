package dicedb.example

import dicedb.client.Client
import dicedb.core.domain.Command

suspend fun main(args: Array<String>) {
    val client = Client("localhost", 7379)
    with(client) {
        val key = "k1"
        val value = "v1"

        // SET
        val setResp = fire(Command.Set(key, value))
        println("Successfully set key $key=$value | $setResp")

        // GET
        val getResponse = fire(Command.Get(key))
        println("Successfully got key $key=${getResponse}")
    }
}
