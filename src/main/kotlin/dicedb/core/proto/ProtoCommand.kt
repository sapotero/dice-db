@file:OptIn(ExperimentalSerializationApi::class)

package dicedb.core.proto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
@SerialName("Command")
data class ProtoCommand(
    @ProtoNumber(1) val cmd: String?,
    @ProtoNumber(2) val args: List<String>
)
