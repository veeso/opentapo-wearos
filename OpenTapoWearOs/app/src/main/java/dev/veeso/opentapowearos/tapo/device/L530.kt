package dev.veeso.opentapowearos.tapo.device

import dev.veeso.opentapowearos.tapo.api.request.TapoRequest
import dev.veeso.opentapowearos.tapo.api.request.params.Color
import dev.veeso.opentapowearos.tapo.api.request.params.GetDeviceInfo
import dev.veeso.opentapowearos.tapo.api.request.params.setdeviceinfo.SetL530DeviceInfo
import dev.veeso.opentapowearos.tapo.api.response.params.TapoResult
import dev.veeso.opentapowearos.tapo.api.response.params.getdeviceinfo.L530InfoResult

class L530(
    ipAddress: String,
    username: String,
    password: String,
) : Device(DeviceType.LIGHT_BULB, DeviceModel.L530, ipAddress, username, password) {

    suspend fun setBrightness(brightness: UInt): Result<TapoResult> {
        val request = SetL530DeviceInfo(null, brightness, null, null, null)
        return setDeviceInfo(request)
    }

    suspend fun setColor(color: Color): Result<TapoResult> {
        val colorCfg = color.getConfig()
        val request =
            SetL530DeviceInfo(null, null, colorCfg.hue, colorCfg.saturation, colorCfg.colorTemp)
        return setDeviceInfo(request)
    }

    suspend fun setHueSaturation(hue: UInt, saturation: UInt): Result<TapoResult> {
        val request = SetL530DeviceInfo(null, null, hue, saturation, null)
        return setDeviceInfo(request)
    }

    suspend fun setColorTemperature(colorTemp: UInt): Result<TapoResult> {
        val request = SetL530DeviceInfo(null, null, null, null, colorTemp)
        return setDeviceInfo(request)
    }

    suspend fun getDeviceInfo(): Result<L530InfoResult> {
        val request = GetDeviceInfo()
        return send(TapoRequest(request), L530InfoResult::class.java).toResult()
    }

}
