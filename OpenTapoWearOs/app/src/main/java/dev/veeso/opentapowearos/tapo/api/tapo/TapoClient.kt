package dev.veeso.opentapowearos.tapo.api.tapo

import android.util.Log
import dev.veeso.opentapowearos.tapo.api.tapo.crypto.Crypter
import dev.veeso.opentapowearos.tapo.api.tapo.crypto.CryptoUtils
import dev.veeso.opentapowearos.tapo.api.tapo.crypto.KeyPair
import dev.veeso.opentapowearos.tapo.api.tapo.request.METHOD_HANDSHAKE
import dev.veeso.opentapowearos.tapo.api.tapo.request.METHOD_LOGIN
import dev.veeso.opentapowearos.tapo.api.tapo.request.METHOD_SECURE_PASSTHROUGH
import dev.veeso.opentapowearos.tapo.api.tapo.request.TapoRequest
import dev.veeso.opentapowearos.tapo.api.tapo.request.params.HandshakeParams
import dev.veeso.opentapowearos.tapo.api.tapo.request.params.LoginParams
import dev.veeso.opentapowearos.tapo.api.tapo.request.params.PassthroughParams
import dev.veeso.opentapowearos.tapo.api.tapo.response.TapoResponse
import dev.veeso.opentapowearos.tapo.api.tapo.response.result.HandshakeResult
import dev.veeso.opentapowearos.tapo.api.tapo.response.result.LoginResult
import dev.veeso.opentapowearos.tapo.api.tapo.response.result.PassthroughResult
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.cookies.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.net.Inet4Address
import java.util.Base64

class TapoClient(ipAddress: Inet4Address) {

    private val url: String
    private val terminalUUID: String = "00-00-00-00-00-00"
    private val client: HttpClient = HttpClient(Android) {
        install(HttpCookies)
    }
    private lateinit var crypter: Crypter

    private var token: String?

    init {
        this.url = String.format("http://%s/app", ipAddress.hostName)
        this.token = null
    }

    suspend fun login(username: String, password: String) {
        Log.d(TAG, String.format("Performing login as %s", username))
        handshake()
        doLogin(username, password)
    }

    private suspend fun handshake() {
        val keypair = KeyPair()
        val request = packRequest(HandshakeParams(keypair.publicPem()))

        Log.d(
            TAG,
            String.format("Performing handshake; public key is: %s", keypair.publicPem())
        )
        val response: TapoResponse<HandshakeResult> = post(request)
        validateResponse(response)
        Log.d(
            TAG,
            String.format("Handshake OK; got key %s; initializing crypter", response.result!!.key)
        )
        this.crypter = Crypter(response.result.key, keypair)
    }

    private suspend fun doLogin(username: String, password: String) {
        Log.d(TAG, String.format("Will login with username %s", username))
        val usernameDigest = CryptoUtils.shaDigestUsername(username)
        val encodedUsername = String(Base64.getEncoder().encode(usernameDigest.toByteArray()))
        val encodedPassword = String(Base64.getEncoder().encode(password.toByteArray()))
        Log.d(TAG, String.format("Encoded username %s => %s", usernameDigest, encodedUsername))

        val request = packRequest(
            LoginParams(encodedUsername, encodedPassword)
        )
        val response: TapoResponse<LoginResult> = passthroughRequest(request)
        validateResponse(response)
        Log.d(TAG, String.format("Login successful; token: %s", response.result!!.token))
        this.token = response.result.token
    }

    private suspend inline fun <reified T, reified U> post(request: TapoRequest<T>): U {
        val serializer = Json { encodeDefaults = true }
        val payload = serializer.encodeToString(request)
        Log.d(TAG, String.format("Sending out payload '%s'", payload))
        val response = client.post(getUrl()) {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }

        Log.d(TAG, String.format("Request status: %d", response.status.value))
        if (response.status.value !in 200..209) {
            throw Exception(
                String.format(
                    "POST failed; expected 200 got %d",
                    response.status.value
                )
            )
        }

        val bodyStr = response.body<String>()
        Log.d(TAG, String.format("Request response: '%s'", bodyStr))

        val deserializer = Json { ignoreUnknownKeys = true }
        return deserializer.decodeFromString(response.body())
    }

    private suspend inline fun <reified T, reified U> passthroughRequest(request: TapoRequest<T>): U {
        val serializer = Json { encodeDefaults = true }
        val payload = serializer.encodeToString(request)
        Log.d(TAG, String.format("Sending out payload to encrypt '%s'", payload))
        val encryptedInnerPayload = this.crypter.encrypt(payload)
        Log.d(TAG, String.format("Encrypted inner payload: %s", encryptedInnerPayload))
        val wrappedRequest = packRequest(
            PassthroughParams(encryptedInnerPayload)
        )
        val response: TapoResponse<PassthroughResult> = post(wrappedRequest)
        validateResponse(response)

        Log.d(TAG, "Decrypting response body")
        val plainResponseBody = crypter.decrypt(response.result!!.response)
        Log.d(TAG, String.format("Plain request response: '%s'", plainResponseBody))

        val deserializer = Json { ignoreUnknownKeys = true }
        return deserializer.decodeFromString(plainResponseBody)
    }

    private fun <T> packRequest(params: T): TapoRequest<T> {
        val millis = System.currentTimeMillis().toUInt()

        val method = when (params!!::class.java) {
            HandshakeParams::class.java -> METHOD_HANDSHAKE
            LoginParams::class.java -> METHOD_LOGIN
            PassthroughParams::class.java -> METHOD_SECURE_PASSTHROUGH
            else ->
                throw Exception("Unknown method")
        }
        return TapoRequest(method, millis, terminalUUID, params)
    }

    private fun <T> validateResponse(response: TapoResponse<T>) {
        if (response.error_code != 0) {
            Log.e(TAG, String.format("Expected error code 0; got %d", response.error_code))
            throw Exception(String.format("Expected error code 0; got %d", response.error_code))
        }
        if (response.result == null) {
            Log.e(TAG, "Expected result not to be null")
            throw Exception("Expected result not to be null")
        }
    }

    private fun getUrl(): String {
        return if (token != null) {
            String.format("%s?token=%s", url, token)
        } else {
            url
        }
    }

    companion object {
        const val TAG = "TapoClient"
    }

}