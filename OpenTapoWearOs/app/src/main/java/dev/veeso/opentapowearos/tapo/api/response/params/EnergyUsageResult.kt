package dev.veeso.opentapowearos.tapo.api.response.params

import dev.veeso.opentapowearos.tapo.api.response.TapoDateTime
import dev.veeso.opentapowearos.tapo.api.response.TapoDateTimeDeserializer

@kotlinx.serialization.Serializable
data class EnergyUsageResult(
    @kotlinx.serialization.Serializable(with = TapoDateTimeDeserializer::class) val local_time: TapoDateTime,
    val current_power: UInt,
    val today_runtime: UInt,
    val today_energy: UInt,
    val month_runtime: UInt,
    val month_energy: UInt,
    val past24h: List<UInt>,
    val past7d: List<List<UInt>>,
    val past30d: List<UInt>,
    val past1y: List<UInt>
) {
}
