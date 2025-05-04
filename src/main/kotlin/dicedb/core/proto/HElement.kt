@file:OptIn(ExperimentalSerializationApi::class)

package dicedb.core.proto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
data class HElement(@ProtoNumber(1) val key: String, @ProtoNumber(2) val value: String)
