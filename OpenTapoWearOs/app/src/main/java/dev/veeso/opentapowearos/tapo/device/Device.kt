package dev.veeso.opentapowearos.tapo.device

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

    var ipAddr: Inet4Address? = null
    private lateinit var client: TpLinkCloudClient // TODO change to TapoClient

    init {
        this.alias = deviceAlias
        this.id = deviceId
        this.macAddress = deviceMac
        this.model = deviceModel
        this.type = deviceType
    }

    fun setIpAddress(ipAddress: Inet4Address) {
        this.ipAddr = ipAddress
        TODO()
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

