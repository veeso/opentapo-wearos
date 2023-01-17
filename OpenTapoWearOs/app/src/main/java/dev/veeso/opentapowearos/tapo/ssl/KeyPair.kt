package dev.veeso.opentapowearos.tapo.ssl

import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.KeyPairGenerator

class KeyPair {

    val private: RSAPrivateKey
    val public: RSAPublicKey

    init {
        val rsa = KeyPairGenerator.getInstance("RSA").generateKeyPair()

        private = rsa.private as RSAPrivateKey
        public = rsa.public as RSAPublicKey
    }

}
