package dev.veeso.opentapowearos.tapo.device

import dev.veeso.opentapowearos.tapo.api.request.TapoRequest

class L520(
    appServerUrl: String,
    token: String,
    deviceAlias: String,
    deviceId: String,
) : Device(appServerUrl, token, deviceAlias, deviceId, DeviceType.LIGHT_BULB, DeviceModel.L510) {

    suspend fun setBrightness(brightness: UInt) {
        TODO("impl")
    }

}
