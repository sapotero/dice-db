package dicedb.client

import dicedb.core.domain.Command
import dicedb.core.domain.Retrier
import dicedb.core.proto.Response
import dicedb.core.proto.Result
import dicedb.core.proto.Status
import dicedb.core.service.ProtobufTCPWire
import dicedb.core.service.WireError
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.Closeable
import java.net.Socket
import java.util.*

class Client(
    host: String,
    port: Int,
    config: ClientConfig = ClientConfig(),
    private val id: String = UUID.randomUUID().toString(),
) : Closeable {
    val mainWire: ProtobufTCPWire =
        ProtobufTCPWire(config.maxMsgSize, Socket(host, port))
    
    val retrier: Retrier =
        Retrier(
            maxRetries = config.maxRetries,
            retryDelay = config.retryDelay,
            retryOn = { it is WireError && it.kind == WireError.Kind.TERMINATED }
        )
    
    private var watchWire: ProtobufTCPWire =
        ProtobufTCPWire(config.maxMsgSize, Socket(host, port))
    
    val mutex = Mutex()
    
    init {
        runBlocking {
            fire(Command.Handshake(id))
        }
    }
    
    suspend inline fun <reified R : Response> fire(command: Command<R>): R =
        mutex.withLock {
            retrier.runWithRetry {
                mainWire.send(command.toProto())?.let { throw it }
                val (result, err) = mainWire.receive<Result>()
                
                if (err != null || result == null || result.status == Status.ERR) {
                    throw err ?: IllegalStateException("Command failed: ${result?.message}")
                }
                
                return@runWithRetry when (R::class) {
                    Response.GETRes::class -> result.getRes
                    Response.SETRes::class -> result.setRes
                    Response.ZADDRes::class -> result.zaddRes
                    Response.ZRANGERes::class -> result.zrangeRes
                    Response.ZRANGEWATCHRes::class -> result.zrangeWatchRes
                    Response.HANDSHAKERes::class -> result.handshakeRes
                    else -> error("Unknown expected response type for ${R::class}")
                } as? R ?: error("Mismatched response type : `${R::class}`")
            }
        }
    
    fun watchFlow(command: Command<*>): Flow<Result> = flow {
        watchWire.send(command.toProto())?.let { throw it }
        while (true) {
            val (msg, err) = watchWire.receive<Result>()
            if (err != null || msg == null) {
                println("Closing watch flow due to error: ${err?.message}")
                watchWire.close()
                break
            }
            emit(msg)
        }
    }.onCompletion {
        watchWire.close()
    }
    
    override fun close() {
        mainWire.close()
        watchWire.close()
    }
}
