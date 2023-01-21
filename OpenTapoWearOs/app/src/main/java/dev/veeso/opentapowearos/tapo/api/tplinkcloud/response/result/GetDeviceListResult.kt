package dev.veeso.opentapowearos.tapo.api.tplinkcloud.response.result

@kotlinx.serialization.Serializable
data class GetDeviceListResult(val deviceList: List<DeviceResult>)

@kotlinx.serialization.Serializable
data class DeviceResult(
    val deviceId: String,
    val appServerUrl: String,
    val alias: String,
    val deviceModel: String,
    val deviceType: String,
    val deviceMac: String,
)
