import dicedb.client.DiceDBClient
import dicedb.core.domain.Command
import dicedb.core.proto.Response

suspend fun main(args: Array<String>) {
    val client = DiceDBClient("localhost", 7379)

    with(client) {
        val key = "k1"
        val value = "v1"

        // SET
        val setResp: Response.SETRes = fire(Command.Set(key, value))
        println("Successfully set key $key=$value | $setResp")

        // GET
        val getResponse: Response.GETRes = fire(Command.Get(key))
        println("Successfully got key $key=${getResponse}")
    }
}
