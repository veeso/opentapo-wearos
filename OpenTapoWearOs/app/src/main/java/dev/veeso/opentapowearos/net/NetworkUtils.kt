package dev.veeso.opentapowearos.net

import android.util.Log
import java.net.Inet4Address
import java.net.UnknownHostException
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.experimental.xor
import kotlin.math.pow


class NetworkUtils {

    companion object {
        fun getNetworkAddress(address: String, netmask: String): Inet4Address {
            val addressBytes = addressToByteArray(address)
            val netmaskBytes = addressToByteArray(netmask)
            val networkBytes = ByteArray(addressBytes.size)

            for (i in networkBytes.indices) {
                networkBytes[i] = addressBytes[i] and netmaskBytes[i]
            }
            return Inet4Address.getByAddress(networkBytes) as Inet4Address
        }

        fun getBroadcastAddress(ipAddress: String, subnetMask: String): Inet4Address {
            val addressBytes = addressToByteArray(ipAddress)
            val netmaskBytes = addressToByteArray(subnetMask)

            val broadcastAddress = ByteArray(addressBytes.size)
            for (i in broadcastAddress.indices) {
                broadcastAddress[i] = (addressBytes[i] or (netmaskBytes[i] xor 255.toByte()))
            }
            return Inet4Address.getByAddress(broadcastAddress) as Inet4Address
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
            Log.d(TAG, String.format("Converting MAC address: %s", macAddress))
            val octets = listOf(
                macAddress.substring(0, 2).lowercase(),
                macAddress.substring(2, 4).lowercase(),
                macAddress.substring(4, 6).lowercase(),
                macAddress.substring(6, 8).lowercase(),
                macAddress.substring(8, 10).lowercase(),
                macAddress.substring(10, 12).lowercase(),
            )

            return octets.joinToString(":")
        }

        fun cidrToNetmask(prefixLength: Int): String {
            val octets = Array(4) { 0 }
            octets[0] = (256 - 2.0.pow(8 - netmaskModule(prefixLength))).toInt()
            if (prefixLength > 8) {
                octets[0] = 255
                octets[1] = (256 - 2.0.pow(8 - netmaskModule(prefixLength - 8))).toInt()
            }
            if (prefixLength > 16) {
                octets[1] = 255
                octets[2] = (256 - 2.0.pow(8 - netmaskModule(prefixLength - 16))).toInt()
            }
            if (prefixLength > 24) {
                octets[2] = 255
                octets[3] = (256 - 2.0.pow(8 - netmaskModule(prefixLength - 24))).toInt()
            }
            return String.format("%s.%s.%s.%s", octets[0], octets[1], octets[2], octets[3])
        }


        private fun netmaskModule(prefixLength: Int): Int {
            val mod = prefixLength % 8
            return if (mod == 0) {
                8
            } else {
                mod
            }
        }

        private fun addressToByteArray(address: String): ByteArray {
            return address.split('.').map {
                octetStrToByte(it)
            }.toByteArray()
        }

        private fun octetStrToByte(octet: String): Byte {
            return octet.toInt().toByte()
        }

        private const val TAG = "NetworkUtils"

    }

}