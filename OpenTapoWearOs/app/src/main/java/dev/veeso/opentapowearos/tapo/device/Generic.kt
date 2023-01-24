package dev.veeso.opentapowearos.tapo.device

class Generic(
    deviceAlias: String,
    deviceId: String,
    endpoint: String,
    ipAddress: String
) : Device(deviceAlias, deviceId, endpoint, ipAddress, DeviceType.UNKNOWN, DeviceModel.GENERIC)
