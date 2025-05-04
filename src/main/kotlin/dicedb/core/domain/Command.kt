package dicedb.core.domain

import dicedb.core.proto.ProtoCommand
import dicedb.core.proto.Response

sealed class Command<R : Response> {
    enum class Channel {
        Command,
        Watch,
    }

    enum class AddFlag {
        NX,
        XX,
        CH,
        INCR,
    }

    enum class SetFlag {
        NX,
        XX,
    }

    enum class ExpireType {
        EX,
        PX,
        EXAT,
        PXAT,
    }

    enum class ZRangeMode(val value: String) {
        BY_RANK("BYRANK"),
        BY_SCORE("BYSCORE"),
    }

    abstract fun toProto(): ProtoCommand

    data class Get(val key: String) : Command<Response.GETRes>() {
        override fun toProto() = ProtoCommand("GET", listOf(key))
    }

    data class Handshake(val clientId: String, val channel: Channel = Channel.Command) :
        Command<Response.HANDSHAKERes>() {
        override fun toProto() = ProtoCommand("HANDSHAKE", listOf(clientId, channel.name))
    }

    data class Decr(val key: String) : Command<Response.DECRRes>() {
        override fun toProto(): ProtoCommand = ProtoCommand("DECR", listOf(key))
    }

    data class DecrBy(val key: String, val decrement: Long) : Command<Response.DECRBYRes>() {
        override fun toProto(): ProtoCommand =
            ProtoCommand("DECRBY", listOf(key, decrement.toString()))
    }

    data class Del(val keys: List<String>) : Command<Response.DELRes>() {
        override fun toProto(): ProtoCommand = ProtoCommand("DEL", keys)
    }

    data class Echo(val message: String) : Command<Response.ECHORes>() {
        override fun toProto(): ProtoCommand = ProtoCommand("ECHO", listOf(message))
    }

    data class Exists(val keys: List<String>) : Command<Response.EXISTSRes>() {
        override fun toProto(): ProtoCommand = ProtoCommand("EXISTS", keys)
    }

    data class Expire(val key: String, val seconds: Long) : Command<Response.EXPIRERes>() {
        override fun toProto(): ProtoCommand =
            ProtoCommand("EXPIRE", listOf(key, seconds.toString()))
    }

    data class ExpireAt(val key: String, val timestamp: Long) : Command<Response.EXPIREATRes>() {
        override fun toProto(): ProtoCommand =
            ProtoCommand("EXPIREAT", listOf(key, timestamp.toString()))
    }

    data class ExpireTime(val key: String) : Command<Response.EXPIRETIMERes>() {
        override fun toProto(): ProtoCommand = ProtoCommand("EXPIRETIME", listOf(key))
    }

    data object FlushDb : Command<Response.FLUSHDBRes>() {
        override fun toProto(): ProtoCommand = ProtoCommand("FLUSHDB", listOf())
    }

    data class GetDel(val key: String) : Command<Response.GETDELRes>() {
        override fun toProto(): ProtoCommand = ProtoCommand("GETDEL", listOf(key))
    }

    data class GetEx(val key: String, val expireType: ExpireType? = null, val time: Long? = null) :
        Command<Response.GETEXRes>() {
        override fun toProto(): ProtoCommand =
            ProtoCommand(
                "GETEX",
                mutableListOf(key).apply {
                    if (expireType != null && time != null) {
                        add(expireType.name)
                        add(time.toString())
                    }
                },
            )
    }

    data class GetSet(val key: String, val value: String) : Command<Response.GETSETRes>() {
        override fun toProto(): ProtoCommand = ProtoCommand("GETSET", listOf(key, value))
    }

    data class GetWatch(val key: String) : Command<Response.GETWATCHRes>() {
        override fun toProto(): ProtoCommand = ProtoCommand("GET.WATCH", listOf(key))
    }

    data class HGet(val key: String, val field: String) : Command<Response.HGETRes>() {
        override fun toProto(): ProtoCommand = ProtoCommand("HGET", listOf(key, field))
    }

    data class HGetAll(val key: String) : Command<Response.HGETALLRes>() {
        override fun toProto(): ProtoCommand = ProtoCommand("HGETALL", listOf(key))
    }

    data class HGetAllWatch(val key: String) : Command<Response.HGETWATCHRes>() {
        override fun toProto() = ProtoCommand("HGETALL.WATCH", listOf(key))
    }

    data class HGetWatch(val key: String, val field: String) : Command<Response.HGETWATCHRes>() {
        override fun toProto(): ProtoCommand = ProtoCommand("HGET.WATCH", listOf(key, field))
    }

    data class HSet(val key: String, val fields: List<String>) : Command<Response.HSETRes>() {
        override fun toProto(): ProtoCommand = ProtoCommand("HSET", fields)
    }

    data class Incr(val key: String) : Command<Response.INCRRes>() {
        override fun toProto() = ProtoCommand("INCR", listOf(key))
    }

    data class IncrBy(val key: String, val increment: Long) : Command<Response.INCRBYRes>() {
        override fun toProto(): ProtoCommand =
            ProtoCommand("INCRBY", listOf(key, increment.toString()))
    }

    data class Keys(val pattern: String) : Command<Response.KEYSRes>() {
        override fun toProto(): ProtoCommand = ProtoCommand("KEYS", listOf(pattern))
    }

    data class Ping(val message: String? = null) : Command<Response.PINGRes>() {
        override fun toProto(): ProtoCommand =
            ProtoCommand("PING", message?.let { listOf(it) } ?: listOf())
    }

    data class Set(
        val key: String,
        val value: String,
        val flag: SetFlag? = null,
        val expireType: ExpireType? = null,
        val expireTime: Long? = null,
        val keepTtl: Boolean = false,
    ) : Command<Response.SETRes>() {
        override fun toProto() =
            ProtoCommand(
                "SET",
                mutableListOf(key, value).apply {
                    flag?.let { add(it.name) }
                    if (expireType != null && expireTime != null) {
                        add(expireType.name)
                        add(expireTime.toString())
                    }
                    if (keepTtl) add("KEEPTTL")
                },
            )
    }

    data class Ttl(val key: String) : Command<Response.TTLRes>() {
        override fun toProto(): ProtoCommand = ProtoCommand("TTL", listOf(key))
    }

    data class Type(val key: String) : Command<Response.TYPERes>() {
        override fun toProto(): ProtoCommand = ProtoCommand("TYPE", listOf(key))
    }

    data object Unwatch : Command<Response.UNWATCHRes>() {
        override fun toProto(): ProtoCommand = ProtoCommand("UNWATCH", emptyList())
    }

    data class ZAdd(
        val key: String,
        val scoreMembers: List<Pair<Int, String>>,
        val flags: List<AddFlag> = emptyList(),
    ) : Command<Response.ZADDRes>() {
        override fun toProto(): ProtoCommand =
            ProtoCommand(
                "ZADD",
                mutableListOf(key).apply {
                    flags.toSet().forEach { flag -> add(flag.name) }
                    scoreMembers.forEach {
                        add(it.first.toString())
                        add(it.second)
                    }
                },
            )
    }

    data class ZCard(val key: String) : Command<Response.ZCARDRes>() {
        override fun toProto(): ProtoCommand = ProtoCommand("ZCARD", listOf(key))
    }

    data class ZCardWatch(val key: String) : Command<Response.ZCARDWATCHRes>() {
        override fun toProto(): ProtoCommand = ProtoCommand("ZCARD.WATCH", listOf(key))
    }

    data class ZCount(val key: String, val min: String, val max: String) :
        Command<Response.ZCARDRes>() {
        override fun toProto(): ProtoCommand = ProtoCommand("ZCOUNT", listOf(key, min, max))
    }

    data class ZCountWatch(val key: String, val min: String, val max: String) :
        Command<Response.ZCOUNTWATCHRes>() {
        override fun toProto(): ProtoCommand = ProtoCommand("ZCOUNT.WATCH", listOf(key, min, max))
    }

    data class ZPopMax(val key: String, val count: Int? = null) : Command<Response.ZPOPMAXRes>() {
        override fun toProto(): ProtoCommand =
            ProtoCommand(
                "ZPOPMAX",
                mutableListOf<String>().apply { if (count != null) add(count.toString()) },
            )
    }

    data class ZPopMin(val key: String, val count: Int? = null) : Command<Response.ZPOPMINRes>() {
        override fun toProto(): ProtoCommand =
            ProtoCommand(
                "ZPOPMIN",
                mutableListOf<String>().apply { if (count != null) add(count.toString()) },
            )
    }

    data class ZRange(
        val key: String,
        val start: Int,
        val stop: Int,
        val mode: ZRangeMode = ZRangeMode.BY_RANK,
    ) : Command<Response.ZRANGERes>() {
        override fun toProto(): ProtoCommand {
            val args = mutableListOf(key, start.toString(), stop.toString())

            if (mode == ZRangeMode.BY_SCORE) args.add(mode.value)
            return ProtoCommand("ZRANGE", args)
        }
    }

    data class ZRangeWatch(
        val key: String,
        val start: Int,
        val stop: Int,
        val mode: ZRangeMode = ZRangeMode.BY_RANK,
    ) : Command<Response.ZRANGEWATCHRes>() {
        override fun toProto(): ProtoCommand {
            val args = mutableListOf(key, start.toString(), stop.toString())
            if (mode == ZRangeMode.BY_SCORE) args.add(mode.value)
            return ProtoCommand("ZRANGE.WATCH", args)
        }
    }

    data class ZRank(val key: String, val member: String) : Command<Response.ZRANKRes>() {
        override fun toProto(): ProtoCommand = ProtoCommand("ZRANK", listOf(key, member))
    }

    data class ZRankWatch(val key: String, val member: String) : Command<Response.ZRANKWATCHRes>() {
        override fun toProto(): ProtoCommand = ProtoCommand("ZRANK.WATCH", listOf(key, member))
    }

    data class ZRem(val key: String, val members: List<String>) : Command<Response.ZREMRes>() {
        override fun toProto(): ProtoCommand = ProtoCommand("ZREM", listOf(key) + members)
    }
}
