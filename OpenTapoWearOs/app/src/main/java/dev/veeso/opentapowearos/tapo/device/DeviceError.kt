package dev.veeso.opentapowearos.tapo.device

class DeviceError(error: DeviceErrorType): Exception(error.message())

enum class DeviceErrorType {
    UNSUPPORTED_FUNCTION;

    fun message(): String {
        return when (this) {
            UNSUPPORTED_FUNCTION -> "Unsupported method"
        }
    }
}
