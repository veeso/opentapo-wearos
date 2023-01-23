package dev.veeso.opentapowearos.tapo.device

class L520(
    deviceAlias: String,
    deviceId: String,
    endpoint: String,
) : Device(deviceAlias, deviceId, endpoint, DeviceType.LIGHT_BULB, DeviceModel.L520) {

    suspend fun setBrightness(brightness: UInt) {
        TODO("impl")
    }

}
