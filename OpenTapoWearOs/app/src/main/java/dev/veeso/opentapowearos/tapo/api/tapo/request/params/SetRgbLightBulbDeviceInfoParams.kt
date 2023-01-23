package dev.veeso.opentapowearos.tapo.api.tapo.request.params

@kotlinx.serialization.Serializable
class SetRgbLightBulbDeviceInfoParams(
    val device_on: Boolean? = null,
    val brightness: UInt? = null,
    val hue: UInt? = null,
    val saturation: UInt? = null,
    val color_temperature: UInt? = null,
)
