package dev.veeso.opentapowearos.tapo.api.tplinkcloud.response

@kotlinx.serialization.Serializable
data class ApiResponse<T>(val error_code: Int, val msg: String? = null, val result: T? = null)
