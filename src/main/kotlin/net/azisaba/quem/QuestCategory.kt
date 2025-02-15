package net.azisaba.quem

import net.azisaba.quem.data.ItemStack
import net.azisaba.quem.data.QuestCategory
import net.azisaba.quem.registry.Keyed
import net.azisaba.quem.extension.toTextComponent
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack

open class QuestCategory(override val key: Key, private val data: QuestCategory): Keyed {
    val title: Component
        get() = data.title.toTextComponent()

    val icon: ItemStack
        get() = ItemStack(data.icon).also {
            val meta = it.itemMeta
            meta.displayName(title)
            it.itemMeta = meta
        }
}