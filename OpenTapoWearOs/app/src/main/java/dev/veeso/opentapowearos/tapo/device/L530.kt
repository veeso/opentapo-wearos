package dev.veeso.opentapowearos.tapo.device

import dev.veeso.opentapowearos.tapo.api.tplinkcloud.request.params.Color

class L530(
    deviceAlias: String,
    deviceId: String,
    deviceMac: String,
) : Device(deviceAlias, deviceId, deviceMac, DeviceType.RGB_LIGHT_BULB, DeviceModel.L530) {

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
