package dev.veeso.opentapowearos.tapo.device

import dev.veeso.opentapowearos.tapo.api.TapoClient

abstract class Device(
    appServerUrl: String,
    token: String,
    deviceAlias: String,
    deviceId: String,
    deviceType: DeviceType,
    deviceModel: DeviceModel,
) {

    val endpoint: String
    val alias: String
    val id: String
    val model: DeviceModel
    val type: DeviceType
    private val client: TapoClient

    init {
        this.endpoint = appServerUrl
        this.alias = deviceAlias
        this.id = deviceId
        this.model = deviceModel
        this.type = deviceType
        this.client = TapoClient(appServerUrl, token)
    }

    suspend fun on() {
        TODO("impl")
    }

    suspend fun off() {
        TODO("impl")
    }

    suspend fun getDeviceUsage() {
        TODO("impl")
    }

    protected suspend fun setDeviceInfo() {
        TODO("impl")
    }

    companion object {
        const val TAG = "Device"
    }

}

