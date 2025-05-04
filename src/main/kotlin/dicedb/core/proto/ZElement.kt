@file:OptIn(ExperimentalSerializationApi::class)

package dicedb.core.proto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
data class ZElement(
    @ProtoNumber(1) val score: Long? = null,
    @ProtoNumber(2) val member: String,
    @ProtoNumber(3) val rank: Long,
)
