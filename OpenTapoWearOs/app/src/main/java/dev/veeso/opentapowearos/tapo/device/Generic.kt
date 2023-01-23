package dev.veeso.opentapowearos.tapo.device

class Generic(
    deviceAlias: String,
    deviceId: String,
    endpoint: String,
) : Device(deviceAlias, deviceId, endpoint, DeviceType.UNKNOWN, DeviceModel.GENERIC)
