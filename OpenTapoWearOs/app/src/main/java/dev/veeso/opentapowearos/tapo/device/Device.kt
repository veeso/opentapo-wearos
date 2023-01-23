package dev.veeso.opentapowearos.tapo.device

import android.util.Log
import dev.veeso.opentapowearos.tapo.api.tapo.TapoClient
import dev.veeso.opentapowearos.tapo.api.tapo.request.params.SetGenericDeviceInfoParams
import dev.veeso.opentapowearos.tapo.api.tplinkcloud.TpLinkCloudClient
import java.net.Inet4Address

abstract class Device(
    deviceAlias: String,
    deviceId: String,
    endpoint: String,
    deviceType: DeviceType,
    deviceModel: DeviceModel,
) {

    val alias: String
    val id: String
    val model: DeviceModel
    val type: DeviceType
    val endpoint: String

    private val client: TapoClient

    init {
        this.alias = deviceAlias
        this.id = deviceId
        this.model = deviceModel
        this.type = deviceType
        this.endpoint = endpoint
        this.client = TapoClient(endpoint)
    }

    suspend fun login(username: String, password: String) {
        this.client.login(username, password)
    }

    suspend fun on() {
        Log.d(TAG, "Powering device ON")
        this.client.setDeviceInfo(SetGenericDeviceInfoParams(device_on = true))
    }

    suspend fun off() {
        Log.d(TAG, "Powering device OFF")
        this.client.setDeviceInfo(SetGenericDeviceInfoParams(device_on = false))
    }

    companion object {
        const val TAG = "Device"
    }

}

