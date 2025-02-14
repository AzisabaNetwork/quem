package net.azisaba.quem.data

import kotlinx.serialization.Serializable

@Serializable
data class Guide(
    val title: String? = null,
    val location: Location,
    val requirements: Map<String, Int> = emptyMap()
)
