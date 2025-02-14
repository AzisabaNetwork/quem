package net.azisaba.quem.util

import net.azisaba.quem.Quem
import net.azisaba.quem.QuestType
import net.azisaba.quem.registry.QuestTypes
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
import org.bukkit.persistence.PersistentDataType

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

private val KEY_QUEST_TYPE_KEY = Key.key(Quem.PLUGIN_ID, "key").toNamespacedKey()
private val KEY_QUEST_TYPE_PLAYS = Key.key(Quem.PLUGIN_ID, "plays").toNamespacedKey()
private val KEY_QUEST_TYPES = Key.key(Quem.PLUGIN_ID, "quest_types").toNamespacedKey()

var Player.questTypeMap: MutableMap<QuestType, Int>
    get() = persistentDataContainer.getOrDefault(KEY_QUEST_TYPES.toNamespacedKey(), PersistentDataType.LIST.dataContainers(), listOf())
        .mapNotNull {
            val key = QuestTypes.get(it.get(KEY_QUEST_TYPE_KEY.toNamespacedKey(), PersistentDataType.STRING)!!.toKey())
            val value = it.get(KEY_QUEST_TYPE_PLAYS, PersistentDataType.INTEGER)
            if (key != null && value != null) key to value else null
        }.toMap().toMutableMap()
    set(value) {
        persistentDataContainer.set(KEY_QUEST_TYPES, PersistentDataType.LIST.dataContainers(), value.map { (type, plays) ->
            val container =persistentDataContainer.adapterContext.newPersistentDataContainer()
            container.set(KEY_QUEST_TYPE_KEY, PersistentDataType.STRING, type.key.asString())
            container.set(KEY_QUEST_TYPE_PLAYS, PersistentDataType.INTEGER, plays)
            container
        })
    }

fun Player.hasPermission(type: QuestType): Boolean {
    val plays = questTypeMap[type]
    val maxPlays = type.maxPlays
    return plays != null && (maxPlays == null || plays < maxPlays)
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