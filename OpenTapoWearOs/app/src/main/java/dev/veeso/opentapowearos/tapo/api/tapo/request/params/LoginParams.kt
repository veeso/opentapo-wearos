package dev.veeso.opentapowearos.tapo.api.tapo.request.params

@kotlinx.serialization.Serializable
data class LoginParams(val username: String, val password: String)
