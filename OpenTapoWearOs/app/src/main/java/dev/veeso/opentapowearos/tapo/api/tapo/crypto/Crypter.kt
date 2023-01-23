package dev.veeso.opentapowearos.tapo.api.tapo.crypto

import android.util.Log
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class Crypter(key: String, keyPair: KeyPair) {

    private val key: ByteArray
    private val iv: ByteArray

    init {
        Log.d(TAG, "Decrypting key...")
        val keyBytes = Base64.getDecoder().decode(key)
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, keyPair.private)
        val decryptedMessage = cipher.doFinal(keyBytes)
        if (decryptedMessage.size != 32) {
            throw Exception(
                String.format(
                    "Expected 32 bytes long key; got %d",
                    decryptedMessage.size
                )
            )
        }
        Log.d(TAG, "Decrypted key")
        this.key = decryptedMessage.slice(0..15).toByteArray()
        this.iv = decryptedMessage.slice(16..31).toByteArray()
        Log.d(TAG, String.format("Key: %s", key))
        Log.d(TAG, String.format("IV: %s", iv))
    }

    fun encrypt(data: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        val iv = IvParameterSpec(iv)

        val secretKeySpec = SecretKeySpec(key, "AES")
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv)

        val encrypted = cipher.doFinal(data.toByteArray())
        return String(Base64.getEncoder().encode(encrypted))
    }

    fun decrypt(secret: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        val iv = IvParameterSpec(iv)

        val secretKeySpec = SecretKeySpec(key, "AES")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, iv)

        val secretBytes = Base64.getDecoder().decode(secret)
        return String(cipher.doFinal(secretBytes))
    }

    companion object {
        const val TAG = "Crypter"
    }

}
