package dev.veeso.opentapowearos.tapo.device

class Generic(
    appServerUrl: String,
    token: String,
    deviceAlias: String,
    deviceId: String,
) : Device(appServerUrl, token, deviceAlias, deviceId, DeviceType.UNKNOWN, DeviceModel.GENERIC)
