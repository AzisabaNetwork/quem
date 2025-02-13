package net.azisaba.quem

import net.azisaba.quem.data.ItemStack
import net.azisaba.quem.data.QuestCategory
import net.azisaba.quem.registry.Keyed
import net.azisaba.quem.util.toTextComponent
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack

open class QuestCategory(override val key: Key, private val data: QuestCategory): Keyed {
    val name: Component
        get() = data.name.toTextComponent()

    val icon: ItemStack
        get() = ItemStack(data.icon).also {
            val meta = it.itemMeta
            meta.displayName(name)
            it.itemMeta = meta
        }
}