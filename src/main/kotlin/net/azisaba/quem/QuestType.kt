package net.azisaba.quem

import net.azisaba.quem.data.ItemStack
import net.azisaba.quem.data.QuestType
import net.azisaba.quem.registry.Keyed
import net.azisaba.quem.registry.QuestCategories
import net.azisaba.quem.util.toKey
import net.azisaba.quem.util.toTextComponent
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Location
import org.bukkit.inventory.ItemStack

class QuestType(override val key: Key, private val data: QuestType): Keyed {
    val name: Component
        get() = data.name.toTextComponent()

    val icon: ItemStack
        get() = ItemStack(data.icon).also {
            val meta = it.itemMeta
            meta.displayName(name.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE))
            meta.lore(description.map { it.colorIfAbsent(NamedTextColor.GRAY).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE) })
            it.itemMeta = meta
        }

    val description: List<Component>
        get() = data.description.map { it.toTextComponent() }

    val category: QuestCategory
        get() = QuestCategories.get(data.category.toKey())!!

    val location: Location
        get() = net.azisaba.quem.data.Location(data.location)

    val maxPlays: Int? = data.maxPlays

    val maxPlayers: Int? = data.maxPlayers

    val minPlayers: Int? = data.minPlayers

    val requirements: Set<QuestRequirement>
        get() = data.requirements.map { QuestRequirement(it.key, it.value) }.toSet()
}