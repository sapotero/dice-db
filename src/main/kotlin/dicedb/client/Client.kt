package dicedb.client

import dicedb.core.domain.Command
import dicedb.core.domain.Retrier
import dicedb.core.proto.Response
import dicedb.core.proto.Result
import dicedb.core.proto.Status
import dicedb.core.service.ProtobufTCPWire
import dicedb.core.service.WireError
import java.io.Closeable
import java.net.Socket
import java.util.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class Client(
    host: String,
    port: Int,
    config: ClientConfig = ClientConfig(),
    private val id: String = UUID.randomUUID().toString(),
) : Closeable {
    val mainWire: ProtobufTCPWire = ProtobufTCPWire(config.maxMsgSize, Socket(host, port))

    val retrier: Retrier =
        Retrier(
            maxRetries = config.maxRetries,
            retryDelay = config.retryDelay,
            retryOn = { it is WireError && it.kind == WireError.Kind.TERMINATED },
        )

    var watchWire: ProtobufTCPWire = ProtobufTCPWire(config.maxMsgSize, Socket(host, port))

    val mutex = Mutex()

    init {
        runBlocking { fire(Command.Handshake(id)) }
    }
    
    /**
    * Fires a command and processes the response, handling retries and error cases.
    *
    * @param command The command to execute, which specifies the request and the expected response type.
    * @return The response of type [R], which is the result of executing the command.
    * @throws IllegalStateException If the command execution fails or the response cannot be mapped to type [R].
    * @throws WireError If there is an error in communication, such as a corrupted message or connection issues.
    */
    suspend inline fun <reified R : Response> fire(command: Command<R>): R =
        mutex.withLock {
            retrier.runWithRetry {
                mainWire.send(command.toProto())?.let { throw it }
                val (result, err) = mainWire.receive<Result>()

                if (err != null || result == null || result.status == Status.ERR) {
                    throw err ?: IllegalStateException("Command failed: ${result?.message}")
                }

                return@runWithRetry mapResponse<R>(result)
            }
        }
    
    /**
    * Establishes a watch flow for the given command, processing incoming messages and emitting responses
    * until an error occurs or the flow completes.
    *
    * @param command The command to be sent to initiate the watch flow.
    * @return A Flow that emits instances of R, which are responses corresponding to the watch command.
    * @throws WireError If there is an error sending the command or receiving responses. Specifically,
    * throws a WireError of kind CORRUPT_MESSAGE if there is an issue decoding the response.
    */
    suspend inline fun <reified R : Response> watchFlow(command: Command<R>): Flow<R> =
        flow {
                watchWire.send(command.toProto())?.let { throw it }
                while (true) {
                    val (msg, err) = watchWire.receive<Result>()
                    if (err != null || msg == null) {
                        println("Closing watch flow due to error: ${err?.message}")
                        watchWire.close()
                        break
                    }
                    emit(mapResponse<R>(msg))
                }
            }
            .onCompletion { watchWire.close() }
    
    /**
    * Maps a [Result] object to a specific response type based on the reified type parameter [R].
    * This function ensures type safety by leveraging Kotlin's reified types, allowing for precise mapping
    * of the response data to the expected type.
    *
    * @param result The [Result] object containing the response data to be mapped.
    *
    * @return An instance of the response type [R] containing the mapped data from the provided [result].
    *
    * @throws IllegalStateException If the response type [R] does not match any known response type.
    * @throws KotlinError If the response type [R] does not match the expected type, indicating a fatal error.
    */
    inline fun <reified R : Response> mapResponse(result: Result): R {
        return when (R::class) {
            Response.TYPERes::class -> result.typeRes
            Response.PINGRes::class -> result.pingRes
            Response.ECHORes::class -> result.echoRes
            Response.HANDSHAKERes::class -> result.handshakeRes
            Response.EXISTSRes::class -> result.existsRes
            Response.GETRes::class -> result.getRes
            Response.SETRes::class -> result.setRes
            Response.DELRes::class -> result.delRes
            Response.KEYSRes::class -> result.keysRes
            Response.GETDELRes::class -> result.getDelRes
            Response.GETEXRes::class -> result.getExRes
            Response.GETSETRes::class -> result.getSetRes
            Response.INCRRes::class -> result.incrRes
            Response.DECRRes::class -> result.decrRes
            Response.INCRBYRes::class -> result.incrByRes
            Response.DECRBYRes::class -> result.decrByRes
            Response.FLUSHDBRes::class -> result.flushDbRes
            Response.EXPIRERes::class -> result.expireRes
            Response.EXPIREATRes::class -> result.expireAtRes
            Response.EXPIRETIMERes::class -> result.expireTimeRes
            Response.TTLRes::class -> result.ttlRes
            Response.GETWATCHRes::class -> result.getWatchRes
            Response.UNWATCHRes::class -> result.unwatchRes
            Response.HGETRes::class -> result.hgetRes
            Response.HSETRes::class -> result.hsetRes
            Response.HGETALLRes::class -> result.hgetAllRes
            Response.HGETWATCHRes::class -> result.hgetWatchRes
            Response.HGETALLWATCHRes::class -> result.hgetAllWatchRes
            Response.ZADDRes::class -> result.zaddRes
            Response.ZCOUNTRes::class -> result.zcountRes
            Response.ZRANGERes::class -> result.zrangeRes
            Response.ZPOPMAXRes::class -> result.zpopMaxRes
            Response.ZREMRes::class -> result.zremRes
            Response.ZPOPMINRes::class -> result.zpopMinRes
            Response.ZRANKRes::class -> result.zrankRes
            Response.ZCARDRes::class -> result.zcardRes
            Response.ZRANGEWATCHRes::class -> result.zrangeWatchRes
            Response.ZCOUNTWATCHRes::class -> result.zcountWatchRes
            Response.ZCARDWATCHRes::class -> result.zcardWatchRes
            Response.ZRANKWATCHRes::class -> result.zrankWatchRes
            else -> throw IllegalStateException("Unknown response type: $result")
        }
            as? R ?: error("Mismatched response type : `${R::class}`")
    }

    override fun close() {
        mainWire.close()
        watchWire.close()
    }
}
