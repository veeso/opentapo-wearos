package dev.veeso.opentapowearos.tapo.api.tapo.response.result.get_device_info

@kotlinx.serialization.Serializable
data class GenericDeviceInfoResult(
    val device_id: String,
    val type: String,
    val model: String,
    val nickname: String,
    val device_on: Boolean,
    val brightness: Int? = null,
    val hue: Int? = null,
    val saturation: Int? = null,
    val color_temp: Int? = null
)
