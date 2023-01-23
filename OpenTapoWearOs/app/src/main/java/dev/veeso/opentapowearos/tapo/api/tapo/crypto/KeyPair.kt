package dev.veeso.opentapowearos.tapo.api.tapo.crypto

import android.security.keystore.KeyProperties
import android.util.Base64.NO_WRAP
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.KeyPair
import java.security.PrivateKey
import android.util.*

class KeyPair {

    private val keypair: KeyPair

    val private: PrivateKey get() = this.keypair.private

    init {
        val generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA)
        generator.initialize(1024, SecureRandom())
        this.keypair = generator.genKeyPair()
    }

    fun publicPem(): String {
        val publicKeyBase64 = String(Base64.encode(this.keypair.public.encoded, NO_WRAP))
        return publicKeyBase64.chunked(64).joinToString(
            separator = "\n",
            prefix = "-----BEGIN PUBLIC KEY-----\n",
            postfix = "\n-----END PUBLIC KEY-----\n"
        )
    }

    fun privatePem(): String {
        val publicKeyBase64 = String(Base64.encode(this.keypair.private.encoded, NO_WRAP))
        return publicKeyBase64.chunked(64).joinToString(
            separator = "\n",
            prefix = "-----BEGIN PRIVATE KEY-----\n",
            postfix = "\n-----END PRIVATE KEY-----\n"
        )
    }

}
