package dev.veeso.opentapowearos.tapo.api.response

import dev.veeso.opentapowearos.tapo.Empty

@kotlinx.serialization.Serializable
data class TapoResponse<T>(val errorCode: Int, val result: T?) {

    fun isOk(): Boolean {
        return errorCode == 0
    }

    fun toResult(): Result<T> {
        return if (isOk() && result != null) {
            Result.success(result)
        } else {
            Result.failure(Exception(String.format("Error: %d", errorCode)))
        }
    }

}
