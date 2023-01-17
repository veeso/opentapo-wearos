package dev.veeso.opentapowearos.tapo.api.request.params.setdeviceinfo

import dev.veeso.opentapowearos.tapo.Empty

interface SetDeviceInfoParams {

    fun validate(): Result<Empty>

}
