package dev.veeso.opentapowearos.tapo.api.response.params.getdeviceinfo

@kotlinx.serialization.Serializable
data class PlugInfoResult(
    val device_id: String,
    val type: String,
    val model: String,
    val hw_id: String,
    val hw_ver: String,
    val fw_id: String,
    val fw_ver: String,
    val oem_id: String,
    val mac: String,
    val ip: String,
    val ssid: String,
    val signal_level: UInt,
    val rssi: Int,
    val specs: String,
    val lang: String,
    val device_on: Boolean,
    val overheated: Boolean,
    val nickname: String,
    val avatar: String,
    val has_set_location_info: Boolean,
    val on_time: UInt?,
    val region: String?,
    val longitude: Int?,
    val latitude: Int?,
    val time_diff: Int?,
)
