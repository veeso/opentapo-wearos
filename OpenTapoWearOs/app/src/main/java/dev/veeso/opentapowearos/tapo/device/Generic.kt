package dev.veeso.opentapowearos.tapo.device

class Generic(
    deviceAlias: String,
    deviceId: String,
    macAddress: String,
) : Device(deviceAlias, deviceId, macAddress, DeviceType.UNKNOWN, DeviceModel.GENERIC)
