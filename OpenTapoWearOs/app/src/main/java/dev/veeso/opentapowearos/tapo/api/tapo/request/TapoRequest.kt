package dev.veeso.opentapowearos.tapo.api.tapo.request

const val METHOD_HANDSHAKE = "handshake"
const val METHOD_LOGIN = "login_device"
const val METHOD_SECURE_PASSTHROUGH = "securePassthrough"
const val METHOD_SET_DEVICE_INFO = "set_device_info"
const val METHOD_GET_DEVICE_INFO = "get_device_info"

@kotlinx.serialization.Serializable
data class TapoRequest<T>(val method: String, val requestTimeMils: UInt, val terminalUuid: String, val params: T)
