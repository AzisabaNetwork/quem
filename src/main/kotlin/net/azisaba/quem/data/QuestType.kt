package net.azisaba.quem.data

import kotlinx.serialization.Serializable

@Serializable
data class QuestType(
    val name: String,
    val icon: Icon,
    val description: List<String>,
    val category: String,
    val location: Location,
    val maxPlays: Int? = null,
    val maxPlayers: Int? = null,
    val minPlayers: Int? = null,
    val requirements: Map<String, Int>
)