package dev.veeso.opentapowearos.view.app_data

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DeviceGroups {

    companion object {
        fun serialize(groups: Map<String, List<String>>): String {
            return Json.encodeToString(groups)
        }

        fun deserialize(payload: String): Map<String, List<String>> {
            return Json.decodeFromString(payload)
        }
    }
}
