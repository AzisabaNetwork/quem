package net.azisaba.quem.data

import kotlinx.serialization.Serializable

@Serializable
data class QuestCategory(
    val name: String,
    val icon: Icon
)
