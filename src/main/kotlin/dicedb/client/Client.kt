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

    private var watchWire: ProtobufTCPWire = ProtobufTCPWire(config.maxMsgSize, Socket(host, port))

    val mutex = Mutex()

    init {
        runBlocking { fire(Command.Handshake(id)) }
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
                    else -> throw IllegalStateException("Unknown response type: ${result}")
                }
                    as? R ?: error("Mismatched response type : `${R::class}`")
            }
        }

    fun watchFlow(command: Command<*>): Flow<Result> =
        flow {
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
            }
            .onCompletion { watchWire.close() }

    override fun close() {
        mainWire.close()
        watchWire.close()
    }
}
