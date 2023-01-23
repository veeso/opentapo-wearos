package dev.veeso.opentapowearos.net

import android.util.Log
import dev.veeso.opentapowearos.tapo.api.tapo.TapoClient
import dev.veeso.opentapowearos.tapo.device.Device
import kotlinx.coroutines.*
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

        // list of address to fetch
        val addressToFetch = mutableListOf<Inet4Address>()
        while (workingAddress != broadcastAddress) {
            addressToFetch.add(workingAddress)
            workingAddress = NetworkUtils.incrementAddress(workingAddress)
        }

        val scanners = addressToFetch.map {
            DeviceScannerWorker(it, username, password)
        }
        // start threads
        Log.d(TAG, String.format("Starting %d workers", scanners.size))
        val jobs = scanners.map {
            val t = Thread(it)
            t.start()
            t
        }
        // join workers
        Log.d(TAG, "Joining workers")
        jobs.forEach {
            it.join()
        }
        // get devices
        scanners.forEach {
            if (it.device != null) {
                this.devices.add(it.device!!)
            }
        }
        Log.d(TAG, String.format("Scan terminated; found %d devices", this.devices.size))
    }

    companion object {
        const val TAG = "IpFinder"
    }
}
