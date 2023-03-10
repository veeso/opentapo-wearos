package dev.veeso.opentapowearos.tapo.device

import android.util.Log
import dev.veeso.opentapowearos.tapo.api.tapo.request.params.SetLightBulbDeviceInfoParams
import dev.veeso.opentapowearos.tapo.api.tapo.request.params.SetRgbLightBulbDeviceInfoParams
import dev.veeso.opentapowearos.view.device_activity.Color

class L530(
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
    DeviceModel.L530,
    deviceStatus
) {

    suspend fun setBrightness(brightness: Int) {
        Log.d(TAG, String.format("Setting brightness to %d", brightness))
        this.client.setDeviceInfo(SetLightBulbDeviceInfoParams(brightness = brightness))
    }

    suspend fun setColor(color: Color) {
        val colorCfg = color.getConfig()
        this.client.setDeviceInfo(
            SetRgbLightBulbDeviceInfoParams(
                hue = colorCfg.hue,
                saturation = colorCfg.saturation,
                color_temp = colorCfg.colorTemp
            )
        )
    }

}
