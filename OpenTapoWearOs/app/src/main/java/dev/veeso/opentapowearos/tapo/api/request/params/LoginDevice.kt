package dev.veeso.opentapowearos.tapo.api.request.params

@kotlinx.serialization.Serializable
data class LoginDevice(val username: String, val password: String) {
}
