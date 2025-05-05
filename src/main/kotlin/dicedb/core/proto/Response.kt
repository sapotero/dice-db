@file:OptIn(ExperimentalSerializationApi::class)

package dicedb.core.proto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
sealed class Response {

    @Serializable
    @SerialName("TYPERes")
    data class TYPERes(@ProtoNumber(1) val type: String = "") : Response()

    @Serializable
    @SerialName("PINGRes")
    data class PINGRes(@ProtoNumber(1) val message: String = "") : Response()

    @Serializable
    @SerialName("ECHORes")
    data class ECHORes(@ProtoNumber(1) val message: String = "") : Response()

    @Serializable
    @SerialName("EXISTSRes")
    data class EXISTSRes(@ProtoNumber(1) val count: Long = 0) : Response()

    @Serializable
    @SerialName("HGETRes")
    data class HGETRes(@ProtoNumber(1) val value: String = "") : Response()

    @Serializable
    @SerialName("HSETRes")
    data class HSETRes(@ProtoNumber(1) val count: Long = 0) : Response()

    @Serializable
    @SerialName("HGETALLRes")
    data class HGETALLRes(@ProtoNumber(1) val elements: List<HElement> = emptyList()) : Response()

    @Serializable
    @SerialName("TTLRes")
    data class TTLRes(@ProtoNumber(1) val seconds: Long = -2) : Response()

    @Serializable
    @SerialName("EXPIRERes")
    data class EXPIRERes(@ProtoNumber(1) val isChanged: Boolean = false) : Response()

    @Serializable
    @SerialName("EXPIREATRes")
    data class EXPIREATRes(@ProtoNumber(1) val isChanged: Boolean = false) : Response()

    @Serializable
    @SerialName("EXPIRETIMERes")
    data class EXPIRETIMERes(@ProtoNumber(1) val unixSec: Long = -2) : Response()

    @Serializable
    @SerialName("GETRes")
    data class GETRes(@ProtoNumber(1) val value: String = "") : Response()

    @Serializable
    @SerialName("GETDELRes")
    data class GETDELRes(@ProtoNumber(1) val value: String? = null) : Response()

    @Serializable
    @SerialName("GETEXRes")
    data class GETEXRes(@ProtoNumber(1) val value: String? = null) : Response()

    @Serializable
    @SerialName("GETSETRes")
    data class GETSETRes(@ProtoNumber(1) val value: String = "") : Response()

    @Serializable
    @SerialName("INCRRes")
    data class INCRRes(@ProtoNumber(1) val value: Long = 0) : Response()

    @Serializable
    @SerialName("DECRRes")
    data class DECRRes(@ProtoNumber(1) val value: Long = 0) : Response()

    @Serializable
    @SerialName("INCRBYRes")
    data class INCRBYRes(@ProtoNumber(1) val value: Long = 0) : Response()

    @Serializable
    @SerialName("DECRBYRes")
    data class DECRBYRes(@ProtoNumber(1) val value: Long = 0) : Response()

    @Serializable
    @SerialName("DELRes")
    data class DELRes(@ProtoNumber(1) val count: Long = 0) : Response()

    @Serializable
    @SerialName("ZADDRes")
    data class ZADDRes(@ProtoNumber(1) val count: Long = 0) : Response()

    @Serializable
    @SerialName("ZCOUNTRes")
    data class ZCOUNTRes(@ProtoNumber(1) val count: Long = 0) : Response()

    @Serializable
    @SerialName("ZREMRes")
    data class ZREMRes(@ProtoNumber(1) val count: Long = 0) : Response()

    @Serializable
    @SerialName("ZCARDRes")
    data class ZCARDRes(@ProtoNumber(1) val count: Long = 0) : Response()

    @Serializable
    @SerialName("ZRANKRes")
    data class ZRANKRes(@ProtoNumber(2) val element: ZElement = ZElement(0, "", 0)) : Response()

    @Serializable
    @SerialName("ZRANGERes")
    data class ZRANGERes(@ProtoNumber(1) val elements: List<ZElement> = emptyList()) : Response()

    @Serializable
    @SerialName("ZPOPMAXRes")
    data class ZPOPMAXRes(@ProtoNumber(1) val elements: List<ZElement> = emptyList()) : Response()

    @Serializable
    @SerialName("ZPOPMINRes")
    data class ZPOPMINRes(@ProtoNumber(1) val elements: List<ZElement> = emptyList()) : Response()

    @Serializable
    @SerialName("KEYSRes")
    data class KEYSRes(@ProtoNumber(1) val keys: List<String> = emptyList()) : Response()

    @Serializable @SerialName("HGETALLWATCHRes") data object HGETALLWATCHRes : Response()

    @Serializable @SerialName("HGETWATCHRes") data object HGETWATCHRes : Response()

    @Serializable @SerialName("GETWATCHRes") data object GETWATCHRes : Response()

    @Serializable @SerialName("UNWATCHRes") data object UNWATCHRes : Response()

    @Serializable @SerialName("HANDSHAKERes") data object HANDSHAKERes : Response()

    @Serializable @SerialName("SETRes") data object SETRes : Response()

    @Serializable @SerialName("FLUSHDBRes") data object FLUSHDBRes : Response()

    @Serializable @SerialName("ZRANGEWATCHRes") data object ZRANGEWATCHRes : Response()

    @Serializable @SerialName("ZCOUNTWATCHRes") data object ZCOUNTWATCHRes : Response()

    @Serializable @SerialName("ZCARDWATCHRes") data object ZCARDWATCHRes : Response()

    @Serializable @SerialName("ZRANKWATCHRes") data object ZRANKWATCHRes : Response()
}
