package dev.veeso.opentapowearos.view.app_data

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

class DeviceCache(devices: List<String>) {

    private var devices: MutableList<String>

    init {
        this.devices = devices.toMutableList()
    }

    constructor(payload: String) : this(listOf()) {
        this.devices = Json.decodeFromString(payload)
    }

    fun serialize(): String {
        return Json.encodeToString(devices)
    }

    fun devices(): List<String> {
        return this.devices.toList()
    }

}
