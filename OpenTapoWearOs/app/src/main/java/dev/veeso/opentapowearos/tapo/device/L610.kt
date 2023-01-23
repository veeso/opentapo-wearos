package dev.veeso.opentapowearos.tapo.device

class L610(
    deviceAlias: String,
    deviceId: String,
    deviceMac: String,
) : Device(deviceAlias, deviceId, deviceMac, DeviceType.LIGHT_BULB, DeviceModel.L610) {

    suspend fun setBrightness(brightness: UInt) {
        TODO("impl")
    }

}
