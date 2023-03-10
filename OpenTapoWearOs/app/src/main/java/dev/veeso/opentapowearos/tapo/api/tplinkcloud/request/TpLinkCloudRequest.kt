package dev.veeso.opentapowearos.tapo.api.tplinkcloud.request

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class TpLinkCloudRequest<T>(val params: T) {

    @SerialName("request_time_mils") var requestTimeMils: Int? = null
    @SerialName("terminal_uuid") var terminalUuid: String? = null

    fun setRequestTimeMils(millis: Int) {
        this.requestTimeMils = millis
    }

    fun setLinkTerminalUuid(uuid: String) {
        this.terminalUuid = uuid
    }

}
