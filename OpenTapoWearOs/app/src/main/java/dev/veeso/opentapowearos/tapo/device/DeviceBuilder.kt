package dev.veeso.opentapowearos.tapo.device

class DeviceBuilder {

    companion object {
        fun buildDevice(
            alias: String,
            deviceId: String,
            model: DeviceModel,
            endpoint: String,
            ipAddress: String
        ): Device {
            return when (model) {
                DeviceModel.GENERIC -> Generic(
                    alias,
                    deviceId,
                    endpoint,
                    ipAddress
                )
                DeviceModel.L510 -> L510(
                    alias,
                    deviceId,
                    endpoint,
                    ipAddress
                )
                DeviceModel.L520 -> L520(
                    alias,
                    deviceId,
                    endpoint,
                    ipAddress
                )
                DeviceModel.L530 -> L530(
                    alias,
                    deviceId,
                    endpoint,
                    ipAddress
                )
                DeviceModel.L610 -> L610(
                    alias,
                    deviceId,
                    endpoint,
                    ipAddress
                )
                DeviceModel.L630 -> L630(
                    alias,
                    deviceId,
                    endpoint,
                    ipAddress
                )
                DeviceModel.P100 -> P100(
                    alias,
                    deviceId,
                    endpoint,
                    ipAddress
                )
                DeviceModel.P110 -> P110(
                    alias,
                    deviceId,
                    endpoint,
                    ipAddress
                )
            }
        }
    }

}
