package dicedb.core.service

import dicedb.core.service.WireError.Kind
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.*
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.concurrent.atomic.AtomicReference

private const val PREFIX_SIZE = 4

enum class Status { OPEN, CLOSED }

class WireError(val kind: Kind, override val cause: Throwable? = null) : Exception(cause) {
    enum class Kind {
        TERMINATED,
        CORRUPT_MESSAGE,
        EMPTY
    }
}

class TCPWire(
    private val maxMsgSize: Int,
    private val socket: Socket
): Wire {
    private val status = AtomicReference(Status.OPEN)
    private val readMutex = Mutex()
    private val writeMutex = Mutex()
    private val input = BufferedInputStream(socket.getInputStream())
    private val output = BufferedOutputStream(socket.getOutputStream())
    
    override suspend fun send(msg: ByteArray): WireError? = writeMutex.withLock {
        if (status.get() == Status.CLOSED) {
            return WireError(Kind.TERMINATED, IllegalStateException("trying to use closed wire"))
        }
        
        val size = msg.size
        val buffer = ByteArray(PREFIX_SIZE + size)
        writePrefix(size, buffer)
        System.arraycopy(msg, 0, buffer, PREFIX_SIZE, size)
        
        return write(buffer)
    }
    
    override suspend fun receive(): Pair<ByteArray?, WireError?> = readMutex.withLock {
        val size = readPrefix() ?: return null to WireError(Kind.EMPTY, IOException("failed to read prefix"))
        
        if (size <= 0) {
            close()
            return null to WireError(Kind.CORRUPT_MESSAGE, IllegalArgumentException("invalid message size: $size"))
        }
        
        if (size > maxMsgSize) {
            close()
            return null to WireError(Kind.CORRUPT_MESSAGE, IllegalArgumentException("message too large: $size > $maxMsgSize"))
        }
        
        return readMessage(size)
    }
    
    override fun close() {
        if (status.compareAndSet(Status.OPEN, Status.CLOSED)) {
            try {
                socket.close()
            } catch (e: IOException) {
                println("Warning: error closing socket: ${e.message}")
            }
        }
    }
    
    private fun writePrefix(size: Int, buffer: ByteArray) {
        val bytes = java.nio.ByteBuffer.allocate(4).putInt(size).array()
        System.arraycopy(bytes, 0, buffer, 0, PREFIX_SIZE)
    }
    
    private fun readPrefix(): Int? {
        val buffer = ByteArray(PREFIX_SIZE)
        var delay = 5L
        val maxRetries = 5
        var lastErr: IOException? = null
        
        repeat(maxRetries) {
            try {
                input.readFully(buffer)
                return java.nio.ByteBuffer.wrap(buffer).int
            } catch (e: IOException) {
                lastErr = e
                if (e is SocketTimeoutException || e.message?.contains("temporary") == true) {
                    Thread.sleep(delay)
                    delay *= 2
                    return@repeat
                }
            }
        }
        
        handleFatalReadError(lastErr)
        return null
    }
    
    private fun handleFatalReadError(e: IOException?) {
        when {
            e == null -> {}
            e is EOFException -> throw WireError(Kind.EMPTY, e)
            e.message?.contains("use of closed network connection") == true -> {
                close()
                throw WireError(Kind.TERMINATED, e)
            }
            else -> {
                close()
                throw WireError(Kind.TERMINATED, e)
            }
        }
    }
    
    private fun readMessage(size: Int): Pair<ByteArray?, WireError?> {
        val buffer = ByteArray(size)
        var delay = 5L
        val maxRetries = 5
        var lastErr: IOException? = null
        
        repeat(maxRetries) {
            try {
                input.readFully(buffer)
                return buffer to null
            } catch (e: IOException) {
                lastErr = e
                if (e is SocketTimeoutException || e.message?.contains("temporary") == true || e is EOFException) {
                    Thread.sleep(delay)
                    delay *= 2
                    return@repeat
                }
            }
        }
        
        status.set(Status.CLOSED)
        return buffer to WireError(Kind.TERMINATED, lastErr)
    }
    
    private fun write(buffer: ByteArray): WireError? {
        var totalWritten = 0
        var partialWriteRetries = 0
        var backoffRetries = 0
        val maxPartialWriteRetries = 10
        val maxBackoffRetries = 5
        var delay = 5L
        var lastRetryableErr: IOException? = null
        
        while (totalWritten < buffer.size) {
            try {
                val toWrite = buffer.copyOfRange(totalWritten, buffer.size)
                output.write(toWrite)
                output.flush()
                totalWritten = buffer.size
            } catch (e: IOException) {
                if (e.message?.contains("closed") == true) {
                    status.set(Status.CLOSED)
                    return WireError(Kind.TERMINATED, e)
                }
                
                if (e is SocketTimeoutException || e.message?.contains("temporary") == true) {
                    if (backoffRetries++ >= maxBackoffRetries) {
                        status.set(Status.CLOSED)
                        return WireError(Kind.TERMINATED, IOException("max backoff retries", e))
                    }
                    Thread.sleep(delay)
                    delay *= 2
                    continue
                }
                
                if (++partialWriteRetries > maxPartialWriteRetries) {
                    status.set(Status.CLOSED)
                    return WireError(Kind.TERMINATED, IOException("max partial write retries", e))
                }
                
                lastRetryableErr = e
                continue
            }
        }
        
        return null
    }
    
    private fun InputStream.readFully(buffer: ByteArray) {
        var read = 0
        while (read < buffer.size) {
            val bytesRead = this.read(buffer, read, buffer.size - read)
            if (bytesRead == -1) throw EOFException("Unexpected EOF while reading stream")
            read += bytesRead
        }
    }
}
