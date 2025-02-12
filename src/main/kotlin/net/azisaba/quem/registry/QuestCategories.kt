package net.azisaba.quem.registry

import net.azisaba.quem.Quem
import net.azisaba.quem.QuestCategory
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.Material

object QuestCategories: Registry<QuestCategory>() {
    val GENERAL = register(QuestCategory(Key.key("${Quem.PLUGIN_ID}:general"), Component.text("General"), Material.CHEST))
    val DAILY = register(QuestCategory(Key.key("${Quem.PLUGIN_ID}:daily"), Component.text("Daily"), Material.CLOCK))
    val STORY = register(QuestCategory(Key.key("${Quem.PLUGIN_ID}:story"), Component.text("Story"), Material.ENCHANTED_BOOK))
}