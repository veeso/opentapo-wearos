package dev.veeso.opentapowearos.tapo.device

class DeviceBuilder {

    companion object {
        fun buildDevice(
            alias: String,
            deviceId: String,
            macAddress: String,
            model: DeviceModel
        ): Device {
            return when (model) {
                DeviceModel.GENERIC -> Generic(
                    alias,
                    deviceId,
                    macAddress
                )
                DeviceModel.L510 -> L510(
                    alias,
                    deviceId,
                    macAddress
                )
                DeviceModel.L520 -> L520(
                    alias,
                    deviceId,
                    macAddress
                )
                DeviceModel.L530 -> L530(
                    alias,
                    deviceId,
                    macAddress
                )
                DeviceModel.L610 -> L610(
                    alias,
                    deviceId,
                    macAddress
                )
                DeviceModel.L630 -> L630(
                    alias,
                    deviceId,
                    macAddress
                )
                DeviceModel.P100 -> P100(
                    alias,
                    deviceId,
                    macAddress
                )
                DeviceModel.P110 -> P110(
                    alias,
                    deviceId,
                    macAddress
                )
            }
        }
    }

}
