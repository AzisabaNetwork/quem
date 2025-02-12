package net.azisaba.quem

import net.azisaba.quem.data.Icon
import net.azisaba.quem.data.ItemStack
import net.azisaba.quem.data.QuestCategory
import net.azisaba.quem.registry.Keyed
import net.azisaba.quem.util.toComponentString
import net.azisaba.quem.util.toKey
import net.azisaba.quem.util.toTextComponent
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class QuestCategory(private val data: QuestCategory): Keyed {
    constructor(key: Key, name: Component, iconType: Material, iconModel: Key? = null, iconAura: Boolean = false) : this(
        QuestCategory(
            key = key.asString(),
            name = name.toComponentString(),
            icon = Icon(
                type = iconType.key().asString(),
                model = iconModel?.asString(),
                aura = iconAura
            )
        )
    )

    override val key: Key
        get() = data.key.toKey()

    val name: Component
        get() = data.name.toTextComponent()

    val icon: ItemStack
        get() = ItemStack(data.icon).also {
            val meta = it.itemMeta
            meta.displayName(name)
            it.itemMeta = meta
        }
}