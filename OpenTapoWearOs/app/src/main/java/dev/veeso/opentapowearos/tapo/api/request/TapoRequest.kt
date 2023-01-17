package dev.veeso.opentapowearos.tapo.api.request

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class TapoRequest<T>(val params: T) {

    @SerialName("request_time_mils") var requestTimeMils: UInt? = null
    @SerialName("terminal_uuid") var terminalUuid: String? = null

    fun setRequestTimeMils(millis: UInt) {
        this.requestTimeMils = millis
    }

    fun setLinkTerminalUuid(uuid: String) {
        this.terminalUuid = uuid
    }

}
