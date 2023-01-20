package dev.veeso.opentapowearos.tapo.device

class DeviceBuilder {

    companion object {
        fun buildDevice(
            appServerUrl: String,
            token: String,
            alias: String,
            deviceId: String,
            model: DeviceModel
        ): Device {
            return when (model) {
                DeviceModel.GENERIC -> Generic(
                    appServerUrl,
                    token,
                    alias,
                    deviceId
                )
                DeviceModel.L510 -> L510(
                    appServerUrl,
                    token,
                    alias,
                    deviceId
                )
                DeviceModel.L520 -> L520(
                    appServerUrl,
                    token,
                    alias,
                    deviceId
                )
                DeviceModel.L530 -> L530(
                    appServerUrl,
                    token,
                    alias,
                    deviceId
                )
                DeviceModel.L610 -> L610(
                    appServerUrl,
                    token,
                    alias,
                    deviceId
                )
                DeviceModel.L630 -> L630(
                    appServerUrl,
                    token,
                    alias,
                    deviceId
                )
                DeviceModel.P100 -> P100(
                    appServerUrl,
                    token,
                    alias,
                    deviceId
                )
                DeviceModel.P110 -> P110(
                    appServerUrl,
                    token,
                    alias,
                    deviceId
                )
            }
        }
    }

}
