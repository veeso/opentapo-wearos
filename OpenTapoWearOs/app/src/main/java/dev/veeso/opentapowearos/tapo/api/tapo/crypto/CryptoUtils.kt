package dev.veeso.opentapowearos.tapo.api.tapo.crypto

import java.security.MessageDigest
import kotlin.experimental.and

class CryptoUtils {

    companion object {
        fun shaDigestUsername(username: String): String {
            val md = MessageDigest.getInstance("SHA-1")
            val textBytes: ByteArray = username.toByteArray()
            md.update(textBytes, 0, textBytes.size)
            val sha1hash: ByteArray = md.digest()
            return convertToHex(sha1hash)

        }

        private fun convertToHex(data: ByteArray): String {
            val buf = StringBuilder()
            for (b in data) {
                var halfByte: Int = (b.toInt() ushr 4 and 0x0F)
                var twoHalves = 0
                do {
                    buf.append(if (halfByte in 0..9) ('0'.code + halfByte).toChar() else ('a'.code + (halfByte - 10)).toChar())
                    halfByte = (b and 0x0F).toInt()
                } while (twoHalves++ < 1)
            }
            return buf.toString()
        }
    }

}
