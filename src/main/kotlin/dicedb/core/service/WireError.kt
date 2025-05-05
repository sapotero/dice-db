package dicedb.core.service

class WireError(val kind: Kind, override val cause: Throwable? = null) : Exception(cause) {
    enum class Kind {
        TERMINATED,
        CORRUPT_MESSAGE,
        EMPTY,
    }
}
