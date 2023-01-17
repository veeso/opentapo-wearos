package dev.veeso.opentapowearos.tapo.api.request.params.setdeviceinfo

import dev.veeso.opentapowearos.tapo.Empty
import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class SetL510DeviceInfo(@SerialName("device_on") val deviceOn: Boolean?, val brightness: UInt?) : SetDeviceInfoParams {

    override fun validate(): Result<Empty> {
        if (brightness != null && brightness > 100u) {
            return Result.failure(Exception("Invalid brightness value. It must be in range 0-100"))
        }
        return Result.success(Empty())
    }

}
