package dev.veeso.opentapowearos.tapo.api

import dev.veeso.opentapowearos.tapo.api.request.TapoRequest
import dev.veeso.opentapowearos.tapo.api.response.TapoResponse
import kotlinx.serialization.json.Json

object Serializer {

    val apiJson = Json {
        TapoRequest::class.java
        TapoResponse::class.java
    }

}
