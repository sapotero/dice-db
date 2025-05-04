@file:OptIn(ExperimentalSerializationApi::class)

package dicedb.core.proto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
enum class Status {
    @ProtoNumber(0) OK,
    @ProtoNumber(1) ERR
}