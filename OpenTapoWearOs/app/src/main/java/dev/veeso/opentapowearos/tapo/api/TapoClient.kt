package dev.veeso.opentapowearos.tapo.api

import android.util.Log
import dev.veeso.opentapowearos.tapo.api.request.ApiRequest
import dev.veeso.opentapowearos.tapo.api.request.DISCOVER_METHOD
import dev.veeso.opentapowearos.tapo.api.request.EmptyParams
import dev.veeso.opentapowearos.tapo.api.request.LOGIN_METHOD
import dev.veeso.opentapowearos.tapo.api.request.params.LoginParams
import dev.veeso.opentapowearos.tapo.api.response.ApiResponse
import dev.veeso.opentapowearos.tapo.api.response.result.GetDeviceListResult
import dev.veeso.opentapowearos.tapo.api.response.result.LoginResult
import dev.veeso.opentapowearos.tapo.device.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.engine.android.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.util.Base64
import java.util.UUID

class TapoClient {

    private var url: String
    private val terminalUUID: String = UUID.randomUUID().toString()
    private val client: HttpClient
    var token: String? = null

    init {
        this.url = BASE_URL
        this.token = null
        this.client = HttpClient(Android)
    }

    constructor() {
        this.url = BASE_URL
        this.token = null
    }

    constructor(apiUrl: String, token: String) {
        this.url = apiUrl
        this.token = token
    }

    suspend fun login(email: String, password: String) {
        val params = ApiRequest(
            LOGIN_METHOD,
            LoginParams(
                email, password, terminalUUID
            )
        )

        val data: ApiResponse<LoginResult> = post(params)

        if (data.error_code == 0 && data.result != null) {
            Log.d(TAG, "Login successful")
            this.token = data.result.token
        } else {
            Log.d(TAG, String.format("Login failed: %d", data.error_code))
            throw Exception(String.format("Login failed: %d", data.error_code))
        }
    }

    suspend fun discoverDevices(): List<Device> {
        val data: ApiResponse<GetDeviceListResult> = post(ApiRequest(DISCOVER_METHOD, EmptyParams()))

        if (data.error_code == 0 && data.result != null) {
            Log.d(
                TAG,
                String.format(
                    "Discovery was successful; found %d devices",
                    data.result.deviceList.size
                )
            )

            return data.result.deviceList.map {
                val model = DeviceModel.fromName(it.deviceModel)
                val alias = String(
                    Base64.getDecoder().decode(it.alias)
                )
                when (model) {
                    DeviceModel.GENERIC -> Generic(
                        it.appServerUrl,
                        this.token!!,
                        alias,
                        it.deviceId
                    )
                    DeviceModel.L510 -> L510(
                        it.appServerUrl,
                        this.token!!,
                        alias,
                        it.deviceId,
                    )
                    DeviceModel.L530 -> L530(
                        it.appServerUrl,
                        this.token!!,
                        alias,
                        it.deviceId,
                    )
                    DeviceModel.P100 -> P100(
                        it.appServerUrl,
                        this.token!!,
                        alias,
                        it.deviceId
                    )
                    DeviceModel.P110 -> P110(
                        it.appServerUrl,
                        this.token!!,
                        alias,
                        it.deviceId
                    )
                }
            }

        } else {
            throw Exception(String.format("Discover devices failed: %d", data.error_code))
        }
    }

    private suspend inline fun <reified T, reified U> post(request: ApiRequest<T>): U {
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

    private fun getUrl(): String {
        return if (this.token == null) {
            this.url
        } else {
            String.format("%s?token=%s", this.url, this.token)
        }
    }

    companion object {
        const val BASE_URL = "https://eu-wap.tplinkcloud.com/"
        const val TAG = "ApiCloudClient"
    }

}
