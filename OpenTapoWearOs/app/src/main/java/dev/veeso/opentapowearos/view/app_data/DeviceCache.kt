package dev.veeso.opentapowearos.view.app_data

import dev.veeso.opentapowearos.tapo.device.Device
import dev.veeso.opentapowearos.tapo.device.DeviceBuilder
import dev.veeso.opentapowearos.tapo.device.DeviceModel
import dev.veeso.opentapowearos.tapo.device.DeviceStatus
import dev.veeso.opentapowearos.view.intent_data.DeviceData
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

class DeviceCache(devices: List<Device>) {

    private var devices: List<Device>

    init {
        this.devices = devices.toMutableList()
    }

    constructor(payload: String) : this(listOf()) {
        val deviceData: List<CachedDevice> = Json.decodeFromString(payload)
        this.devices = deviceData.map {
            DeviceBuilder.buildDevice(
                it.alias,
                it.id,
                it.model,
                it.endpoint,
                it.ipAddress,
                it.status
            )
        }

    }

    fun serialize(): String {
        val serialized: List<CachedDevice> = this.devices.map {
            CachedDevice(
                it.alias,
                it.id,
                it.model,
                it.endpoint,
                it.ipAddress,
                it.status
            )
        }
        return Json.encodeToString(serialized)
    }

    fun devices(): List<Device> {
        return this.devices
    }

}

@kotlinx.serialization.Serializable
data class CachedDevice(
    val alias: String,
    val id: String,
    val model: DeviceModel,
    val endpoint: String,
    val ipAddress: String,
    val status: DeviceStatus
)
