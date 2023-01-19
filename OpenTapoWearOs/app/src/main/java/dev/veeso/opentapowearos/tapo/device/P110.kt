package dev.veeso.opentapowearos.tapo.device

import dev.veeso.opentapowearos.tapo.api.request.TapoRequest

class P110(
    appServerUrl: String,
    token: String,
    deviceAlias: String,
    deviceId: String,
) : Device(appServerUrl, token, deviceAlias, deviceId, DeviceType.PLUG, DeviceModel.P110) {


}
