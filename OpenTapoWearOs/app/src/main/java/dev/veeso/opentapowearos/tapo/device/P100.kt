package dev.veeso.opentapowearos.tapo.device

import dev.veeso.opentapowearos.tapo.api.request.TapoRequest
import dev.veeso.opentapowearos.tapo.api.request.params.GetDeviceInfo
import dev.veeso.opentapowearos.tapo.api.response.params.getdeviceinfo.GenericDeviceInfoResult

class P100(
    ipAddress: String,
    username: String,
    password: String,
) : Device(DeviceType.PLUG, DeviceModel.P100, ipAddress, username, password) {

    suspend fun getDeviceInfo(): Result<GenericDeviceInfoResult> {
        val request = GetDeviceInfo()
        return send(TapoRequest(request), GenericDeviceInfoResult::class.java).toResult()
    }

}
