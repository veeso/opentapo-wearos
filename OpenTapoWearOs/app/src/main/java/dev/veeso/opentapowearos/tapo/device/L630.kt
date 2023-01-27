package dev.veeso.opentapowearos.tapo.device

import android.util.Log
import dev.veeso.opentapowearos.tapo.api.tapo.request.params.SetLightBulbDeviceInfoParams
import dev.veeso.opentapowearos.tapo.api.tapo.request.params.SetRgbLightBulbDeviceInfoParams
import dev.veeso.opentapowearos.view.device_activity.Color

class L630(
    deviceAlias: String,
    deviceId: String,
    endpoint: String,
    ipAddress: String,
    deviceStatus: DeviceStatus,
) : Device(
    deviceAlias,
    deviceId,
    endpoint,
    ipAddress,
    DeviceType.RGB_LIGHT_BULB,
    DeviceModel.L630,
    deviceStatus
) {

    suspend fun setBrightness(brightness: Int) {
        Log.d(TAG, String.format("Setting brightness to %d", brightness))
        this.client.setDeviceInfo(SetLightBulbDeviceInfoParams(brightness = brightness))
    }

    suspend fun setColor(color: Color) {
        val colorCfg = color.getConfig()
        val colorTemp = colorCfg.colorTemp ?: 0
        this.client.setDeviceInfo(
            SetRgbLightBulbDeviceInfoParams(
                hue = colorCfg.hue,
                saturation = colorCfg.saturation,
                color_temp = colorTemp
            )
        )
    }

    suspend fun setHueSaturation(hue: Int, saturation: Int) {
        TODO("impl")
    }

    suspend fun setColorTemperature(colorTemp: Int) {
        TODO("impl")
    }

}
