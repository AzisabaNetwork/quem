package net.azisaba.quem.data

import net.azisaba.quem.util.toNamespacedKey
import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

@Serializable
data class Icon(
    val type: String,
    val model: String? = null,
    val aura: Boolean = false
)

fun ItemStack(icon: Icon): ItemStack {
    val item = ItemStack(Material.matchMaterial(icon.type) ?: Material.CHEST)
    val meta = item.itemMeta

    icon.model?.let {
        meta.itemModel = it.toNamespacedKey()
    }

    if (icon.aura) {
        meta.addEnchant(Enchantment.INFINITY, 1, false)
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
    }

    return item.also { it.itemMeta = meta }
}