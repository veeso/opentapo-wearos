package dev.veeso.opentapowearos.view.app_data

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

class DeviceCache {

    companion object {
        fun serialize(devices: List<String>): String {
            return Json.encodeToString(devices)
        }

        fun deserialize(payload: String): List<String> {
            return Json.decodeFromString(payload)
        }
    }

}
