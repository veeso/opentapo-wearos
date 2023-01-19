package dev.veeso.opentapowearos.tapo.api

import dev.veeso.opentapowearos.tapo.api.request.TapoRequest
import dev.veeso.opentapowearos.tapo.api.response.ApiResponse
import kotlinx.serialization.json.Json

object Serializer {

    val apiJson = Json {
        TapoRequest::class.java
        ApiResponse::class.java
    }

}
