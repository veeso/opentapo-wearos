package dev.veeso.opentapowearos.net

import android.util.Log
import dev.veeso.opentapowearos.tapo.device.Device
import java.net.Inet4Address
import kotlin.math.ceil


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
        doScanNetwork(buildNetworkAddressList(deviceIp, deviceMask))
    }

    private fun doScanNetwork(addressToFetch: List<Inet4Address>) {
        val maxWorkers = ceil(addressToFetch.size / 16.0).toInt()
        val scanners = addressToFetch.chunked(maxWorkers).map {
            Log.d(TAG, String.format("Created a scanner with %d address to scan (%s)", it.size, it))
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
            this.devices.addAll(it.devices)
        }
        Log.d(TAG, String.format("Scan terminated; found %d devices", this.devices.size))
    }

    private fun buildNetworkAddressList(deviceIp: String, deviceMask: String): List<Inet4Address> {
        val networkAddress = NetworkUtils.getNetworkAddress(deviceIp, deviceMask)
        Log.d(TAG, String.format("Found network address: %s", networkAddress))
        val broadcastAddress = NetworkUtils.getBroadcastAddress(deviceIp, deviceMask)
        Log.d(TAG, String.format("Found broadcast address: %s", broadcastAddress))
        Log.d(TAG, String.format("Scanning network %s", networkAddress))
        var workingAddress = NetworkUtils.incrementAddress(networkAddress)

        val addressToFetch = mutableListOf<Inet4Address>()

        while (workingAddress != broadcastAddress) {
            addressToFetch.add(workingAddress)
            workingAddress = NetworkUtils.incrementAddress(workingAddress)
        }

        return addressToFetch
    }

    companion object {
        const val TAG = "IpFinder"
    }
}
