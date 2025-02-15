package net.azisaba.quem.extension

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.entity.Player

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

fun Player.navigateTo(location: Location, renderDistance: Double, gap: Double) {
    val playerLocation = this.location
    val distance = playerLocation.distance(location)

    var i = 0.0

    while (i <= distance) {
        val x = playerLocation.x + (location.x - playerLocation.x) * (i / distance)
        val y = playerLocation.y + (location.y - playerLocation.y) * (i / distance)
        val z = playerLocation.z + (location.z - playerLocation.z) * (i / distance)

        val particleLocation = Location(location.world, x, y, z)

        i += gap

        if (renderDistance <= particleLocation.distance(particleLocation)) {
            break
        }

        spawnParticle(Particle.DUST, particleLocation, 1, Particle.DustOptions(Color.AQUA, 0.5F))
    }
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