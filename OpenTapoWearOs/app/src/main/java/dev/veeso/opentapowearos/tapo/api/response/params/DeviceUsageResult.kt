package dev.veeso.opentapowearos.tapo.api.response.params

@kotlinx.serialization.Serializable
data class DeviceUsage(
    val time_usage: UsageByPeriod,
    val power_usage: UsageByPeriod,
    val saved_power: UsageByPeriod
)

@kotlinx.serialization.Serializable
data class UsageByPeriod(val today: UInt, val past7: UInt, val past30: UInt)
