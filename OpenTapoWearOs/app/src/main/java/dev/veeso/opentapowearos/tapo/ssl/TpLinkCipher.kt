package dev.veeso.opentapowearos.tapo.ssl

import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class TpLinkCipher(keyString: String, keyPair: KeyPair) {

    private val key: ByteArray
    private val iv: ByteArray

    init {
        val keyBytes = Base64.getDecoder().decode(keyString)

        val cipher = Cipher.getInstance("RSA")
        cipher.init(Cipher.DECRYPT_MODE, keyPair.private)
        val decryptedKey = cipher.doFinal(keyBytes)

        if (decryptedKey.size != 32) {
            throw Exception(String.format("expected 32 bytes, got %d", decryptedKey.size))
        }


        this.key = decryptedKey.copyOfRange(0, 16)
        this.iv = decryptedKey.copyOfRange(16, 32)
    }

    fun encrypt(data: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        val iv = IvParameterSpec(this.iv)
        val secretKeySpec = SecretKeySpec(key, "AES")

        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv)
        val encrypted = cipher.doFinal(data.toByteArray(Charsets.UTF_8))

        return String(Base64.getEncoder().encode(encrypted))
    }

    fun decrypt(secret: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        val iv = IvParameterSpec(this.iv)
        val secretKeySpec = SecretKeySpec(key, "AES")

        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, iv)
        val data = cipher.doFinal(Base64.getDecoder().decode(secret))

        return String(data)

    }

}
