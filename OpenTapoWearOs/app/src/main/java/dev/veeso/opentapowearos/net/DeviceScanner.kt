package dev.veeso.opentapowearos.net

import android.util.Log
import dev.veeso.opentapowearos.tapo.api.tapo.TapoClient
import dev.veeso.opentapowearos.tapo.device.Device
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.Inet4Address


class DeviceScanner(username: String, password: String) {

    private val username: String
    private val password: String
    val devices: MutableList<Device>

    init {
        this.username = username
        this.password = password
        this.devices = mutableListOf()
    }

    fun scanNetwork(deviceIp: String, deviceMask: String) {

        //val networkAddress = NetworkUtils.getNetworkAddress(deviceIp, deviceMask)
        val networkAddress = Inet4Address.getByName("192.168.178.34") as Inet4Address
        Log.d(TAG, String.format("Found network address: %s", networkAddress))
        val broadcastAddress = Inet4Address.getByName("192.168.178.44" )as Inet4Address
        //val broadcastAddress = NetworkUtils.getBroadcastAddress(deviceIp, deviceMask)
        Log.d(TAG, String.format("Found broadcast address: %s", broadcastAddress))
        Log.d(TAG, String.format("Scanning network %s", networkAddress))

        var workingAddress = NetworkUtils.incrementAddress(networkAddress)

        while (workingAddress != broadcastAddress) {
            // skip same address
            Log.d(TAG, String.format("Trying to connect to %s", workingAddress))
            if (workingAddress.isReachable(1000)) {
                Log.d(TAG, String.format("Device at %s is reachable", workingAddress))
                // handshake device
                val device = tryToConnectToDevice(workingAddress)
                if (device != null) {
                    Log.d(TAG, String.format("Found TP link device at %s", workingAddress))
                    this.devices.add(device)
                }
            }
            // increment address
            workingAddress = NetworkUtils.incrementAddress(workingAddress)
        }
    }

    private fun tryToConnectToDevice(address: Inet4Address): Device? {
        val client = TapoClient(address)
        var device: Device? = null
        runBlocking {
            withContext(Dispatchers.IO) {
                try {
                    client.login(username, password)
                    Log.d(TAG, "Successfully signed in to device; getting device info...")

                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e(TAG, String.format("Login failed: %s", e))
                }
                try {
                    device = client.queryDevice()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e(TAG, "Query device failed: %s", e)
                }
            }
        }
        return device
    }

    companion object {
        const val TAG = "IpFinder"
    }
}
