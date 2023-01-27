package dev.veeso.opentapowearos.view.app_data

import android.util.Log
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DeviceGroups {

    private var groups: MutableMap<String, List<String>>

    init {
        this.groups = mutableMapOf()
    }

    constructor(payload: String) {
        this.groups = Json.decodeFromString(payload)
    }

    fun add(name: String, devices: List<String>) {
        if (groups.contains(name)) {
            throw Exception(String.format("Group '%s' already exists", name))
        }
        Log.d(TAG, String.format("Adding group %s with devices %s", name, devices))
        groups[name] = devices
    }

    fun remove(name: String) {
        Log.d(TAG, String.format("Removing group %s", name))
        groups.remove(name)
    }

    fun serialize(): String {
        return Json.encodeToString(groups)
    }

    fun getNames(): List<String> {
        return groups.keys.toList()
    }

    fun getDevices(name: String): List<String> {
        if (!groups.contains(name)) {
            throw Exception(String.format("Group '%s' doesn't exist", name))
        }
        return groups[name]!!
    }

    companion object {
        const val TAG = "DeviceGroups"
    }
}
