package dev.veeso.opentapowearos.tapo.device

import dev.veeso.opentapowearos.tapo.api.tapo.TapoClient
import dev.veeso.opentapowearos.tapo.api.tplinkcloud.TpLinkCloudClient
import java.net.Inet4Address

abstract class Device(
    deviceAlias: String,
    deviceId: String,
    deviceMac: String,
    deviceType: DeviceType,
    deviceModel: DeviceModel,
) {

    val alias: String
    val id: String
    val macAddress: String
    val model: DeviceModel
    val type: DeviceType

    private lateinit var client: TapoClient

    init {
        this.alias = deviceAlias
        this.id = deviceId
        this.macAddress = deviceMac
        this.model = deviceModel
        this.type = deviceType
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

