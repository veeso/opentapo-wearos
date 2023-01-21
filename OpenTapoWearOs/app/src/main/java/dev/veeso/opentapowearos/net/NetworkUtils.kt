package dev.veeso.opentapowearos.net

import java.net.Inet4Address
import java.net.UnknownHostException
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.experimental.xor


class NetworkUtils {

    companion object {
        fun getNetworkAddress(address: String, netmask: String): Inet4Address {
            val ipAddress = address.toByteArray()
            val netmaskBytes = netmask.toByteArray()
            val networkAddress = ByteArray(ipAddress.size)

            for (i in networkAddress.indices) {
                networkAddress[i] = ipAddress[i] and netmaskBytes[i]
            }
            return Inet4Address.getByName(networkAddress.toString()) as Inet4Address
        }

        fun getBroadcastAddress(ipAddress: String, subnetMask: String): Inet4Address {
            val ipAddressBytes = ipAddress.toByteArray()
            val subnetMaskBytes = subnetMask.toByteArray()

            val broadcastAddress = ByteArray(ipAddressBytes.size)
            for (i in broadcastAddress.indices) {
                broadcastAddress[i] = (ipAddressBytes[i] or (subnetMaskBytes[i] xor 255.toByte()))
            }
            return Inet4Address.getByName(broadcastAddress.toString()) as Inet4Address
        }

        fun incrementAddress(address: Inet4Address): Inet4Address {
            val newAddress: ByteArray = address.address
            for (i in newAddress.indices.reversed()) {
                if (++newAddress[i] != 0x00.toByte()) break
            }
            return Inet4Address.getByAddress(newAddress) as Inet4Address
        }

        /**
         * Converts a MAC address with TAPO syntax (e.g. 11CE6AE69422) to correct notation (11:ce:6a:e6:94:22)
         */
        fun convertMacFromTapo(macAddress: String): String {
            val octets = listOf(
                macAddress.substring(0, 2).lowercase(),
                macAddress.substring(2, 2).lowercase(),
                macAddress.substring(4, 2).lowercase(),
                macAddress.substring(6, 2).lowercase(),
                macAddress.substring(8, 2).lowercase(),
                macAddress.substring(10, 2).lowercase(),
            )

            return octets.joinToString(":")
        }

    }

}