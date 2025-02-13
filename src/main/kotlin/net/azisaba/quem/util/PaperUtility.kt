package net.azisaba.quem.util

import net.azisaba.quem.Quem
import net.azisaba.quem.QuestType
import net.azisaba.quem.registry.QuestTypes
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.NamespacedKey
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

fun String.toKey(): Key {
    return Key.key(this)
}

fun String.toNamespacedKey(): NamespacedKey {
    return toKey().toNamespacedKey()
}

fun String.toTextComponent(): TextComponent {
    return LegacyComponentSerializer.legacySection().deserialize(this.replace("&", "§"))
}