package dev.veeso.opentapowearos.tapo.api.tapo.request.params

@kotlinx.serialization.Serializable
data class SetLightBulbDeviceInfoParams(
    val device_on: Boolean? = null,
    val brightness: UInt? = null
)
