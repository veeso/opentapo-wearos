package dev.veeso.opentapowearos.tapo.device

import dev.veeso.opentapowearos.tapo.api.request.TapoRequest
import dev.veeso.opentapowearos.tapo.api.request.params.Color

class L530(
    appServerUrl: String,
    token: String,
    deviceAlias: String,
    deviceId: String,
) : Device(appServerUrl, token, deviceAlias, deviceId, DeviceType.LIGHT_BULB, DeviceModel.L530) {

    suspend fun setBrightness(brightness: UInt) {
        TODO("impl")
    }

    suspend fun setColor(color: Color) {
        val colorCfg = color.getConfig()
        TODO("impl")
    }

    suspend fun setHueSaturation(hue: UInt, saturation: UInt) {
        TODO("impl")
    }

    suspend fun setColorTemperature(colorTemp: UInt) {
        TODO("impl")
    }

}
