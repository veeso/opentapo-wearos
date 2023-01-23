package dev.veeso.opentapowearos.tapo.device

class L510(
    deviceAlias: String,
    deviceId: String,
    endpoint: String,
) : Device(deviceAlias, deviceId, endpoint, DeviceType.LIGHT_BULB, DeviceModel.L510) {

    suspend fun setBrightness(brightness: UInt) {
        TODO("impl")
    }

}
