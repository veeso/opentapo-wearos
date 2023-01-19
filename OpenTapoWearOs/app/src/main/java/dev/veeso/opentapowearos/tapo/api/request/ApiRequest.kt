package dev.veeso.opentapowearos.tapo.api.request

const val LOGIN_METHOD = "login"
const val DISCOVER_METHOD = "getDeviceList"

@kotlinx.serialization.Serializable
data class ApiRequest<T>(val method: String, val params: T)

@kotlinx.serialization.Serializable
class EmptyParams()
