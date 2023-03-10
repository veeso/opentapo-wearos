package dev.veeso.opentapowearos.tapo.device

class P100(
    deviceAlias: String,
    deviceId: String,
    endpoint: String,
    ipAddress: String,
    deviceStatus: DeviceStatus,
) : Device(
    deviceAlias,
    deviceId,
    endpoint,
    ipAddress,
    DeviceType.PLUG,
    DeviceModel.P100,
    deviceStatus
)
