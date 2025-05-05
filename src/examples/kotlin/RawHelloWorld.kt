import dicedb.core.proto.ProtoCommand
import dicedb.core.service.ProtobufTCPWire
import java.net.Socket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

const val maxResponseSize = 32 * 1024 * 1024

suspend fun main(args: Array<String>) {
    val socket = withContext(Dispatchers.IO) { Socket("127.0.0.1", 7379) }

    val wire = ProtobufTCPWire(maxResponseSize, socket)
    val err = wire.send(ProtoCommand("GET", listOf("k1")))
    if (err != null) {
        error(err)
    }
    val (response, recvErr) = wire.receive<ProtoCommand>()

    println("response: $response | err: $recvErr")
}
