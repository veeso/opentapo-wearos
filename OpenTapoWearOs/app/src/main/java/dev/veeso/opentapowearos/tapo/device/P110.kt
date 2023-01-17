package dev.veeso.opentapowearos.tapo.device

import dev.veeso.opentapowearos.tapo.api.request.TapoRequest
import dev.veeso.opentapowearos.tapo.api.request.params.GetDeviceInfo
import dev.veeso.opentapowearos.tapo.api.request.params.GetEnergyUsage
import dev.veeso.opentapowearos.tapo.api.response.params.EnergyUsageResult
import dev.veeso.opentapowearos.tapo.api.response.params.getdeviceinfo.GenericDeviceInfoResult

class P110(
    ipAddress: String,
    username: String,
    password: String,
) : Device(DeviceType.PLUG, DeviceModel.P110, ipAddress, username, password) {

    suspend fun getEnergyUsage(): Result<EnergyUsageResult> {
        val request = TapoRequest(GetEnergyUsage())
        return send(request, EnergyUsageResult::class.java).toResult()
    }

    suspend fun getDeviceInfo(): Result<GenericDeviceInfoResult> {
        val request = GetDeviceInfo()
        return send(TapoRequest(request), GenericDeviceInfoResult::class.java).toResult()
    }

}
