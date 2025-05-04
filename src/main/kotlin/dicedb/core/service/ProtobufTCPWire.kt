@file:OptIn(ExperimentalSerializationApi::class, ExperimentalSerializationApi::class)

package dicedb.core.service

import java.net.Socket
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

class ProtobufTCPWire(maxMsgSize: Int, socket: Socket) {
    val tcpWire: Wire = TCPWire(maxMsgSize, socket)

    suspend inline fun <reified T> send(msg: T): WireError? {
        return try {
            val bytes = ProtoBuf.encodeToByteArray(msg)
            tcpWire.send(bytes)
        } catch (e: Exception) {
            tcpWire.close()
            WireError(WireError.Kind.CORRUPT_MESSAGE, e)
        }
    }

    suspend inline fun <reified T> receive(): Pair<T?, WireError?> {
        val (buffer, err) = tcpWire.receive()
        if (err != null || buffer == null) return null to err

        return try {
            val message = ProtoBuf.decodeFromByteArray<T>(buffer)
            message to null
        } catch (e: Exception) {
            tcpWire.close()
            null to WireError(WireError.Kind.CORRUPT_MESSAGE, e)
        }
    }

    fun close() = tcpWire.close()
}
