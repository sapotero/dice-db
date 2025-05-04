package dicedb.core.service

interface Wire {
    suspend fun send(data: ByteArray): WireError?
    suspend fun receive(): Pair<ByteArray?, WireError?>
    fun close()
}
