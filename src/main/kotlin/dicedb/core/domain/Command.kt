package dicedb.core.domain

import dicedb.core.proto.ProtoCommand
import dicedb.core.proto.Response

sealed class Command<R : Response> {
    sealed class Channel(val value: String) {
        data object Command : Channel("command")
        data object Watch : Channel("watch")
    }
    
    
    abstract fun toProto(): ProtoCommand
    
    data class Get(val key: String) : Command<Response.GETRes>() {
        override fun toProto() = ProtoCommand("GET", listOf(key))
    }
    
    data class Set(val key: String, val value: String) : Command<Response.SETRes>() {
        override fun toProto() = ProtoCommand("SET", listOf(key, value))
    }
    
    data class ZAdd(
        val key: String,
        val score: Int,
        val member: String,
        val flags: List<AddFlag> = emptyList()
    ) : Command<Response.ZADDRes>() {
        
        enum class AddFlag {
            NX,
            XX,
            CH
        }
        
        override fun toProto(): ProtoCommand {
            val args = mutableListOf(key, score.toString(), member)
            flags.forEach { args.add(it.name) }
            return ProtoCommand("ZADD", args)
        }
    }
    
    data class ZRange(
        val key: String,
        val start: Int,
        val stop: Int,
        val mode: ZRangeMode = ZRangeMode.BY_RANK,
        val withScores: Boolean = false
    ) : Command<Response.ZRANGERes>() {
        override fun toProto(): ProtoCommand {
            val args = mutableListOf(key, start.toString(), stop.toString())
            
            if (mode == ZRangeMode.BY_SCORE) args.add(mode.value)
            if (withScores) args.add("WITHSCORES")
            
            return ProtoCommand("ZRANGE", args)
        }
    }
    
    data class ZRangeWatch(
        val key: String,
        val start: Int,
        val stop: Int,
        val byRank: Boolean = false
//    ) : Command<Response.ZRANGEWATCHRes>() {
    ) : Command<Response.ZRANGERes>() {
        override fun toProto(): ProtoCommand {
            val args = mutableListOf(key, start.toString(), stop.toString())
            if (byRank) args.add("BYRANK")
            return ProtoCommand("ZRANGE.WATCH", args)
        }
    }
    
    data class Handshake(val clientId: String, val channel: Channel = Channel.Command) :
        Command<Response.HANDSHAKERes>() {
        override fun toProto() = ProtoCommand("HANDSHAKE", listOf(clientId, channel.value))
    }
    
    enum class ZRangeMode(val value: String) {
        BY_RANK("BYRANK"),
        BY_SCORE("BYSCORE")
    }
}