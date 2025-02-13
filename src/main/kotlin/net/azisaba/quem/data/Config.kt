package net.azisaba.quem.data

import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val maxPartySize: Int,
    val lobby: Location? = null,
    val panel: Panel,
)