package net.azisaba.quem.data

import kotlinx.serialization.Serializable

@Serializable
data class QuestCategory(
    val title: String,
    val icon: Icon
)
