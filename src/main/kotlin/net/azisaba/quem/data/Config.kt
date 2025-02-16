package net.azisaba.quem.data

import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val maxPartySize: Int,
    val partyInviteLimit: Long = 20L * 60,
    val lobby: Location? = null,
    val panel: Panel,
)