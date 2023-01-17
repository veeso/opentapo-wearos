package dev.veeso.opentapowearos.tapo.api.request.params.setdeviceinfo

import dev.veeso.opentapowearos.tapo.Empty
import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class SetGenericDeviceInfo(@SerialName("device_on") val deviceOn: Boolean) : SetDeviceInfoParams {

    override fun validate(): Result<Empty> {
        return Result.success(Empty())
    }

}
