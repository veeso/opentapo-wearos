package dev.veeso.opentapowearos.tapo.device

class DeviceBuilder {

    companion object {
        fun buildDevice(
            alias: String,
            deviceId: String,
            model: DeviceModel,
            endpoint: String,
        ): Device {
            return when (model) {
                DeviceModel.GENERIC -> Generic(
                    alias,
                    deviceId,
                    endpoint,
                )
                DeviceModel.L510 -> L510(
                    alias,
                    deviceId,
                    endpoint,
                )
                DeviceModel.L520 -> L520(
                    alias,
                    deviceId,
                    endpoint,
                )
                DeviceModel.L530 -> L530(
                    alias,
                    deviceId,
                    endpoint,
                )
                DeviceModel.L610 -> L610(
                    alias,
                    deviceId,
                    endpoint,
                )
                DeviceModel.L630 -> L630(
                    alias,
                    deviceId,
                    endpoint,
                )
                DeviceModel.P100 -> P100(
                    alias,
                    deviceId,
                    endpoint,
                )
                DeviceModel.P110 -> P110(
                    alias,
                    deviceId,
                    endpoint,
                )
            }
        }
    }

}
