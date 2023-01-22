package dev.veeso.opentapowearos.net

import android.util.Log
import java.io.BufferedReader
import java.io.FileReader
import java.net.Inet4Address


class IpFinder(deviceMacList: List<String>) {

    private val deviceMacList: List<String>
    val hosts: MutableMap<String, Inet4Address>

    init {
        this.deviceMacList = deviceMacList
        this.hosts = mutableMapOf()
    }

    fun scanNetwork(deviceIp: String, deviceMask: String) {

        val devicesToFind = deviceMacList.toMutableList()
        val networkAddress = NetworkUtils.getNetworkAddress(deviceIp, deviceMask)
        Log.d(TAG, String.format("Found network address: %s", networkAddress))
        val broadcastAddress = NetworkUtils.getBroadcastAddress(deviceIp, deviceMask)
        Log.d(TAG, String.format("Found broadcast address: %s", broadcastAddress))
        Log.d(TAG, String.format("Scanning network %s", networkAddress))

        var workingAddress = NetworkUtils.incrementAddress(networkAddress)

        while (workingAddress != broadcastAddress && devicesToFind.isNotEmpty()) {
            // skip same address
            Log.d(TAG, String.format("Getting MAC for %s", workingAddress))
            if (workingAddress.isReachable(1000)) {
                Log.d(TAG, String.format("Device at %s is reachable", workingAddress))
                val macAddress = getMacAddressFromIP(workingAddress.hostName)
                Log.d(TAG, String.format("%s has MAC %s", workingAddress, macAddress))
                if (macAddress != null && devicesToFind.contains(macAddress)) {
                    Log.d(TAG, String.format("Found MAC %s for %s", macAddress, workingAddress))
                    this.hosts[macAddress] =
                        Inet4Address.getByName(workingAddress.hostName) as Inet4Address
                    // remove mac from devices to find
                    devicesToFind.remove(macAddress)
                }
            }
            // increment address
            workingAddress = NetworkUtils.incrementAddress(workingAddress)
            Log.d(TAG, String.format("%d devices to find remaining", devicesToFind.size))
        }
    }

    private fun getMacAddressFromIP(ipFinding: String): String? {
        var bufferedReader: BufferedReader? = null
        try {
            bufferedReader = BufferedReader(FileReader("/proc/net/arp"))
            var line: String
            while (bufferedReader.readLine().also { line = it } != null) {
                Log.d(TAG, String.format("Parsing line '%s'", line))
                val split = line.split(" +").toTypedArray()
                if (split.size >= 4) {
                    val ip = split[0]
                    val mac = split[3]
                    Log.d(TAG, String.format("Checking ip '%s' with mac '%s'", ip, mac))
                    if (mac.matches(MAC_REGEX)) {
                        if (ip.equals(ipFinding, ignoreCase = true)) {
                            return mac
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, String.format("Failed to read ARP table: %s", e.message))
            return null
        } finally {
            bufferedReader?.close()
        }
        return null
    }

    companion object {
        const val TAG = "IpFinder"
        val MAC_REGEX = Regex("([0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}")
    }
}