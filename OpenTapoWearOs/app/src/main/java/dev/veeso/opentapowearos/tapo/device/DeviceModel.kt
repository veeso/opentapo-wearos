package dev.veeso.opentapowearos.tapo.device

enum class DeviceModel {
    L510,
    L520,
    L530,
    L610,
    L630,
    P100,
    P110,
    GENERIC;

    companion object {
        fun fromName(name: String): DeviceModel {
            return if (name.startsWith("L510")) {
                L510
            } else if (name.startsWith("L520")) {
                L520
            } else if (name.startsWith("L530")) {
                L530
            } else if (name.startsWith("L610")) {
                L610
            } else if (name.startsWith("L630")) {
                L630
            } else if (name.startsWith("P100")) {
                P100
            } else if (name.startsWith("P110")) {
                P110
            } else {
                GENERIC
            }
        }
    }

}
