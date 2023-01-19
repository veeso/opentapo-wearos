package dev.veeso.opentapowearos.tapo.api.response

@kotlinx.serialization.Serializable
data class ApiResponse<T>(val error_code: Int, val result: T? = null)
