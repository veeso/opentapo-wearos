package dev.veeso.opentapowearos.tapo.device

import android.util.Log
import dev.veeso.opentapowearos.tapo.Empty
import dev.veeso.opentapowearos.tapo.api.request.TapoRequest
import dev.veeso.opentapowearos.tapo.api.request.params.GetDeviceUsage
import dev.veeso.opentapowearos.tapo.api.request.params.Handshake
import dev.veeso.opentapowearos.tapo.api.request.params.LoginDevice
import dev.veeso.opentapowearos.tapo.api.request.params.SecurePassthrough
import dev.veeso.opentapowearos.tapo.api.request.params.setdeviceinfo.SetDeviceInfoParams
import dev.veeso.opentapowearos.tapo.api.request.params.setdeviceinfo.SetGenericDeviceInfo
import dev.veeso.opentapowearos.tapo.api.response.TapoResponse
import dev.veeso.opentapowearos.tapo.api.response.params.DeviceUsage
import dev.veeso.opentapowearos.tapo.api.response.params.HandshakeResult
import dev.veeso.opentapowearos.tapo.api.response.params.TapoResult
import dev.veeso.opentapowearos.tapo.api.response.params.TokenResult
import dev.veeso.opentapowearos.tapo.ssl.KeyPair
import dev.veeso.opentapowearos.tapo.ssl.TpLinkCipher
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.util.Optional

abstract class Device(
    deviceType: DeviceType,
    deviceModel: DeviceModel,
    ipAddress: String,
    username: String,
    password: String,
) {

    val deviceType: DeviceType
    val deviceModel: DeviceModel
    private var client: HttpClient
    private val url: String
    private val username: String
    private val password: String
    private lateinit var tpLinkCipher: TpLinkCipher
    private var token: String?

    init {
        this.deviceType = deviceType
        this.deviceModel = deviceModel
        this.client = HttpClient {
            install(HttpCookies)
        }
        this.url = String.format("http://%s/app", ipAddress)
        this.username = username
        this.password = password
        this.token = null
    }

    suspend fun login(): Result<Empty> {
        this.client = HttpClient {
            install(HttpCookies)
        }

        val result = handshake()
        if (result.isFailure) {
            return result
        }
        return sendLoginRequest()
    }

    suspend fun on(): Result<TapoResult> {
        val request = SetGenericDeviceInfo(true)
        return setDeviceInfo(request)
    }

    suspend fun off(): Result<TapoResult> {
        val request = SetGenericDeviceInfo(false)
        return setDeviceInfo(request)
    }

    suspend fun getDeviceUsage(): Result<DeviceUsage> {
        val request = GetDeviceUsage()
        return send(TapoRequest(request), DeviceUsage::class.java).toResult()
    }

    protected suspend fun <T, U> send(
        request: TapoRequest<T>,
        decodeAs: Class<U>
    ): TapoResponse<U> {
        val payload = Json.encodeToString(request)
        Log.d(TAG, String.format("Sending out payload: %s", payload))

        val securePassthrough = SecurePassthrough(tpLinkCipher.encrypt(payload))

        val url = if (this.token != null) {
            String.format("%s?token=%s", this.url, this.token)
        } else {
            this.url
        }
        Log.d(TAG, String.format("Sending request to", url))

        val response = client.post(url) {
            setBody(securePassthrough)
            contentType(ContentType.Application.Json)
        }

        if (response.status.value in 200..299) {
            return TapoResponse(response.status.value, null)
        }

        val bytes: ByteArray = response.body()
        val responseStr = tpLinkCipher.decrypt(bytes.toString())
        Log.d(TAG, String.format("Response from device: %s", responseStr))

        return Json.decodeFromString(responseStr)
    }

    private suspend fun handshake(): Result<Empty> {
        Log.d(TAG, "Performing handshake...")
        val keypair = KeyPair()

        val params = Handshake(keypair.public.toString())
        val request = TapoRequest(params)

        val payload = Json.encodeToString(request)
        Log.d(TAG, String.format("Sending out payload: %s", payload))

        val response = client.post(url) {
            setBody(payload)
            contentType(ContentType.Application.Json)
        }

        if (response.status.value in 200..299) {
            return Result.failure(
                Exception(
                    String.format(
                        "Bad HTTP status: %d",
                        response.status.value
                    )
                )
            )
        }

        val bytes: ByteArray = response.body()

        val handshakeResult: HandshakeResult = Json.decodeFromString(bytes.toString())

        val key = handshakeResult.key
        tpLinkCipher = TpLinkCipher(key, keypair)

        return Result.success(Empty())
    }

    private suspend fun sendLoginRequest(): Result<Empty> {
        Log.d(TAG, String.format("Signing in with username '%s'", username))
        val params = LoginDevice(username, password)
        val request = TapoRequest(params)

        val result = send(request, TokenResult::class.java).toResult()

        if (result.isFailure) {
            return Result.failure(result.exceptionOrNull()!!)
        }

        this.token = result.getOrNull()!!.token

        return Result.success(Empty())
    }

    protected suspend fun setDeviceInfo(params: SetDeviceInfoParams): Result<TapoResult> {
        val validation = params.validate()
        if (validation.isFailure) {
            return Result.failure(validation.exceptionOrNull()!!)
        }

        val request = TapoRequest(params)
        return send(request, TapoResult::class.java).toResult()
    }

    companion object {
        const val TAG = "Device"
    }

}

