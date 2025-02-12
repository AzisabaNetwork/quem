package net.azisaba.quem.util

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.NamespacedKey

val Component.plainText: String
    get() = PlainTextComponentSerializer.plainText().serialize(this)

fun Component.toComponentString(): String {
    return LegacyComponentSerializer.legacySection().serialize(this).replace("§", "&")
}

fun Key.toNamespacedKey(): NamespacedKey {
    return NamespacedKey(namespace(), value())
}

fun NamespacedKey.toKey(): Key {
    return Key.key(namespace, key)
}

fun String.toKey(): Key {
    return Key.key(this)
}

fun String.toNamespacedKey(): NamespacedKey {
    return toKey().toNamespacedKey()
}

fun String.toTextComponent(): TextComponent {
    return LegacyComponentSerializer.legacySection().deserialize(this.replace("&", "§"))
}