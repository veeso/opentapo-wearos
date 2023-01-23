package dev.veeso.opentapowearos.net

import android.util.Log
import dev.veeso.opentapowearos.tapo.api.tapo.TapoClient
import dev.veeso.opentapowearos.tapo.device.Device
import dev.veeso.opentapowearos.view.Credentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
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

        val networkAddress = NetworkUtils.getNetworkAddress(deviceIp, deviceMask)
        Log.d(TAG, String.format("Found network address: %s", networkAddress))
        val broadcastAddress = NetworkUtils.getBroadcastAddress(deviceIp, deviceMask)
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
                }
            }
            // increment address
            workingAddress = NetworkUtils.incrementAddress(workingAddress)
        }
    }

    private fun tryToConnectToDevice(address: Inet4Address): Device? {
        val client = TapoClient(address)
        runBlocking {
            withContext(Dispatchers.IO) {
                try {
                    client.login(username, password)
                    Log.d(TAG, "Successfully signed in to device; getting device info...")
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e(TAG, String.format("Login failed: %s", e))
                }
            }
        }
        return null
    }

    companion object {
        const val TAG = "IpFinder"
    }
}
