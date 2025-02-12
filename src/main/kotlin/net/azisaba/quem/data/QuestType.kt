package net.azisaba.quem.data

import kotlinx.serialization.Serializable

@Serializable
data class QuestType(
    val name: String,
    val description: List<String>,
    val icon: Icon,
    val requirements: Map<String, Int>,
    val category: String,
    val location: Location
)