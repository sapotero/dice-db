@file:OptIn(ExperimentalSerializationApi::class)

package dicedb.core.proto

import dicedb.core.proto.Response.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
data class Result(
    @ProtoNumber(1) val status: Status?,
    @ProtoNumber(2) val message: String,
    @ProtoNumber(3) val fingerprint64: ULong?,

    // need to use flatten list instead of
    // @ProtoNumber(11) val response: Response

    @ProtoNumber(11) val typeRes: TYPERes? = null,
    @ProtoNumber(12) val pingRes: PINGRes? = null,
    @ProtoNumber(13) val echoRes: ECHORes? = null,
    @ProtoNumber(14) val handshakeRes: HANDSHAKERes? = null,
    @ProtoNumber(15) val existsRes: EXISTSRes? = null,
    @ProtoNumber(16) val getRes: GETRes? = null,
    @ProtoNumber(17) val setRes: SETRes? = null,
    @ProtoNumber(18) val delRes: DELRes? = null,
    @ProtoNumber(19) val keysRes: KEYSRes? = null,
    @ProtoNumber(20) val getDelRes: GETDELRes? = null,
    @ProtoNumber(21) val getExRes: GETEXRes? = null,
    @ProtoNumber(22) val getSetRes: GETSETRes? = null,
    @ProtoNumber(23) val incrRes: INCRRes? = null,
    @ProtoNumber(24) val decrRes: DECRRes? = null,
    @ProtoNumber(25) val incrByRes: INCRBYRes? = null,
    @ProtoNumber(26) val decrByRes: DECRBYRes? = null,
    @ProtoNumber(27) val flushDbRes: FLUSHDBRes? = null,
    @ProtoNumber(28) val expireRes: EXPIRERes? = null,
    @ProtoNumber(29) val expireAtRes: EXPIREATRes? = null,
    @ProtoNumber(30) val expireTimeRes: EXPIRETIMERes? = null,
    @ProtoNumber(31) val ttlRes: TTLRes? = null,
    @ProtoNumber(32) val getWatchRes: GETWATCHRes? = null,
    @ProtoNumber(33) val unwatchRes: UNWATCHRes? = null,
    @ProtoNumber(34) val hgetRes: HGETRes? = null,
    @ProtoNumber(35) val hsetRes: HSETRes? = null,
    @ProtoNumber(36) val hgetAllRes: HGETALLRes? = null,
    @ProtoNumber(37) val hgetWatchRes: HGETWATCHRes? = null,
    @ProtoNumber(38) val hgetAllWatchRes: HGETALLWATCHRes? = null,
    @ProtoNumber(39) val zaddRes: ZADDRes? = null,
    @ProtoNumber(40) val zcountRes: ZCOUNTRes? = null,
    @ProtoNumber(41) val zrangeRes: ZRANGERes? = null,
    @ProtoNumber(42) val zpopMaxRes: ZPOPMAXRes? = null,
    @ProtoNumber(43) val zremRes: ZREMRes? = null,
    @ProtoNumber(44) val zpopMinRes: ZPOPMINRes? = null,
    @ProtoNumber(45) val zrankRes: ZRANKRes? = null,
    @ProtoNumber(46) val zcardRes: ZCARDRes? = null,
    @ProtoNumber(47) val zrangeWatchRes: ZRANGEWATCHRes? = null,
    @ProtoNumber(48) val zcountWatchRes: ZCOUNTWATCHRes? = null,
    @ProtoNumber(49) val zcardWatchRes: ZCARDWATCHRes? = null,
    @ProtoNumber(50) val zrankWatchRes: ZRANKWATCHRes? = null,
)
