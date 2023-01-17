package dev.veeso.opentapowearos.tapo.api.response

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TapoDateTime(datetime: LocalDateTime) {

    val datetime: LocalDateTime

    init {
        this.datetime = datetime
    }

}

object TapoDateTimeDeserializer : KSerializer<TapoDateTime> {

    private const val DATETIME_SYNTAX: String = "yyyy-MM-dd HH:mm:ss"

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("TapoDateTime", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): TapoDateTime {
        val str = decoder.decodeString()
        val formatter = DateTimeFormatter.ofPattern(DATETIME_SYNTAX)
        val date = LocalDateTime.parse(str, formatter)

        return TapoDateTime(date)
    }

    override fun serialize(encoder: Encoder, value: TapoDateTime) {
        val formatter = DateTimeFormatter.ofPattern(DATETIME_SYNTAX)

        val str = value.datetime.format(formatter)
        encoder.encodeString(str)
    }

}
