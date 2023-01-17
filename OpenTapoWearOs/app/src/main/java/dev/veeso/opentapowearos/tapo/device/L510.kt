package dev.veeso.opentapowearos.tapo.device

import dev.veeso.opentapowearos.tapo.api.request.TapoRequest
import dev.veeso.opentapowearos.tapo.api.request.params.GetDeviceInfo
import dev.veeso.opentapowearos.tapo.api.request.params.setdeviceinfo.SetL510DeviceInfo
import dev.veeso.opentapowearos.tapo.api.response.params.TapoResult
import dev.veeso.opentapowearos.tapo.api.response.params.getdeviceinfo.L510InfoResult

class L510(
    ipAddress: String,
    username: String,
    password: String,
) : Device(DeviceType.LIGHT_BULB, DeviceModel.L510, ipAddress, username, password) {

    suspend fun setBrightness(brightness: UInt): Result<TapoResult> {
        val request = SetL510DeviceInfo(null, brightness)
        return setDeviceInfo(request)
    }

    suspend fun getDeviceInfo(): Result<L510InfoResult> {
        val request = GetDeviceInfo()
        return send(TapoRequest(request), L510InfoResult::class.java).toResult()
    }

}
