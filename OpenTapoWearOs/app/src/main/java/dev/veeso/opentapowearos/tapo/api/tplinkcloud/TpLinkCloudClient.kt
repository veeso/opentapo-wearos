package dev.veeso.opentapowearos.tapo.api.tplinkcloud

import android.util.Log
import dev.veeso.opentapowearos.tapo.api.tplinkcloud.request.ApiRequest
import dev.veeso.opentapowearos.tapo.api.tplinkcloud.request.LOGIN_METHOD
import dev.veeso.opentapowearos.tapo.api.tplinkcloud.request.params.LoginParams
import dev.veeso.opentapowearos.tapo.api.tplinkcloud.response.ApiResponse
import dev.veeso.opentapowearos.tapo.api.tplinkcloud.response.result.LoginResult
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.engine.android.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.util.UUID

class TpLinkCloudClient {

    private val url: String = BASE_URL
    private val terminalUUID: String = UUID.randomUUID().toString()
    private val client: HttpClient = HttpClient(Android)
    var token: String? = null

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
