package dev.veeso.opentapowearos.tapo.api.tapo.response.result.get_device_info

@kotlinx.serialization.Serializable
data class GenericDeviceInfoResult(
    val device_id: String,
    val type: String,
    val model: String,
    val nickname: String,
)
