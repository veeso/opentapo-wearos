package dev.veeso.opentapowearos.tapo.device

import android.util.Log
import dev.veeso.opentapowearos.tapo.api.tapo.request.params.SetLightBulbDeviceInfoParams

class L510(
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
    DeviceType.LIGHT_BULB,
    DeviceModel.L510,
    deviceStatus
) {

    suspend fun setBrightness(brightness: Int) {
        Log.d(TAG, String.format("Setting brightness to %d", brightness))
        this.client.setDeviceInfo(SetLightBulbDeviceInfoParams(brightness = brightness))
    }

}
