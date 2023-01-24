package dev.veeso.opentapowearos.tapo.device

import android.util.Log
import dev.veeso.opentapowearos.tapo.api.tapo.TapoClient
import dev.veeso.opentapowearos.tapo.api.tapo.request.params.SetGenericDeviceInfoParams

abstract class Device(
    deviceAlias: String,
    deviceId: String,
    endpoint: String,
    ipAddress: String,
    deviceType: DeviceType,
    deviceModel: DeviceModel,
    deviceStatus: DeviceStatus
) {

    val alias: String
    val id: String
    val model: DeviceModel
    val type: DeviceType
    val endpoint: String
    val ipAddress: String
    var status: DeviceStatus

    protected val client: TapoClient

    init {
        this.alias = deviceAlias
        this.id = deviceId
        this.model = deviceModel
        this.type = deviceType
        this.endpoint = endpoint
        this.client = TapoClient(endpoint)
        this.ipAddress = ipAddress
        this.status = deviceStatus
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

    suspend fun getDeviceStatus(): DeviceStatus {
        Log.d(TAG, "Getting device info")
        val deviceInfo = this.client.getDeviceInfo()
        this.status = DeviceStatus(
            deviceOn = deviceInfo.device_on,
            brightness = deviceInfo.brightness,
            hue = deviceInfo.hue,
            saturation = deviceInfo.saturation,
            colorTemperature = deviceInfo.color_temp
        )
        return this.status
    }

    companion object {
        const val TAG = "Device"
    }

}

