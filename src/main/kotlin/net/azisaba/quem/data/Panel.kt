package net.azisaba.quem.data

import kotlinx.serialization.Serializable

@Serializable
data class Panel(
    val title: String,
    val footer: String
)
