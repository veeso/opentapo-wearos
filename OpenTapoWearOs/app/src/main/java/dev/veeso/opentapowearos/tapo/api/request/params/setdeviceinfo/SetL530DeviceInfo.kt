package dev.veeso.opentapowearos.tapo.api.request.params.setdeviceinfo

import dev.veeso.opentapowearos.tapo.Empty
import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class SetL530DeviceInfo(
    @SerialName("device_on") val deviceOn: Boolean?,
    val brightness: UInt?,
    val hue: UInt?,
    val saturation: UInt?,
    @SerialName("color_temp") val colorTemp: UInt?
) : SetDeviceInfoParams {

    override fun validate(): Result<Empty> {
        if (brightness != null && brightness > 100u) {
            return Result.failure(Exception("Invalid brightness value. It must be in range 0-100"))
        }

        if (
            colorTemp != null &&
            (hue != null || saturation != null)
        ) {
            return Result.failure(Exception("Color temperature cannot be set along with hue and saturation"))
        }

        if ((hue == null && saturation != null) || (hue != null && saturation == null)) {
            return Result.failure(Exception("Hue and saturation must be both set or unset"))
        }

        if (hue != null && hue > 360u) {
            return Result.failure(Exception("Invalid hue value. It must be in range 0-360"))
        }

        if (saturation != null && saturation > 100u) {
            return Result.failure(Exception("Invalid saturation value. It must be in range 0-100"))
        }

        if (colorTemp != null && (colorTemp < 2500u || colorTemp > 6500u)) {
            return Result.failure(Exception("Invalid color temperature value. It must be in range 2500-6500"))
        }

        return Result.success(Empty())
    }
}
