package dev.veeso.opentapowearos.tapo.api.tplinkcloud.request.params

import kotlinx.serialization.Serializable

@Serializable
data class LoginParams(
    val cloudUserName: String,
    val cloudPassword: String,
    val terminalUUID: String,
    val appType: String = "Tapo_Android"
)
