package net.azisaba.quem.data

import kotlinx.serialization.Serializable

@Serializable
data class Stage(
    val title: String,
    val location: Location,
    val unmountLocation: Location? = null,
    val maxParties: Int = 1
)
