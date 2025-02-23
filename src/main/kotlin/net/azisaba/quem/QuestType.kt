package net.azisaba.quem

import net.azisaba.quem.data.ItemStack
import net.azisaba.quem.data.QuestType
import net.azisaba.quem.registry.Keyed
import net.azisaba.quem.registry.QuestCategories
import net.azisaba.quem.extension.toKey
import net.azisaba.quem.extension.toNamespacedKey
import net.azisaba.quem.extension.toTextComponent
import net.azisaba.quem.registry.QuestTypes
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

class QuestType(override val key: Key, private val data: QuestType): Keyed {
    val title: Component
        get() = data.title.toTextComponent()

    val icon: ItemStack
        get() = ItemStack(data.icon).also {
            val meta = it.itemMeta
            meta.displayName(title.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE))
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

    val guides: List<Guide> = data.guides.map { Guide( it) }.toList()

    val requirements: Set<QuestRequirement> = data.requirements.map { QuestRequirement(it.key, it.value) }.toSet()

    val scripts: Set<Script> = data.scripts.map { Script.create(it.key, it.value) }.toSet()
        get() = field.toSet()

    fun hasPlayLimit(): Boolean {
        return maxPlays != null
    }

    fun fireTrigger(trigger: Script.Trigger, quest: Quest) {
        scripts.filter { it.trigger == trigger }.forEach { it.run(quest) }
    }
}

private val KEY_QUEST_TYPE_KEY = Key.key(Quem.PLUGIN_ID, "key").toNamespacedKey()
private val KEY_QUEST_TYPE_PLAYS = Key.key(Quem.PLUGIN_ID, "plays").toNamespacedKey()
private val KEY_QUEST_TYPES = Key.key(Quem.PLUGIN_ID, "types").toNamespacedKey()

private val KEY_DAILY_TIMESTAMP = Key.key(Quem.PLUGIN_ID, "timestamp").toNamespacedKey()
private val KEY_DAILY_TYPES = Key.key(Quem.PLUGIN_ID, "types").toNamespacedKey()
private val KEY_DAILY = Key.key(Quem.PLUGIN_ID, "daily").toNamespacedKey()

val Player.questTypes: Set<net.azisaba.quem.QuestType>
    get() = questTypeMap.keys

var Player.questTypeMap: MutableMap<net.azisaba.quem.QuestType, Int>
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

val Player.dailyQuestTypes: Set<net.azisaba.quem.QuestType>
    get() {
        var newDataContainer = false

        val pdc = persistentDataContainer.get(KEY_DAILY, PersistentDataType.TAG_CONTAINER) ?: run {
            newDataContainer = true

            val container = persistentDataContainer.adapterContext.newPersistentDataContainer()
            persistentDataContainer.set(KEY_DAILY, PersistentDataType.TAG_CONTAINER, container)
            container
        }

        val now = Calendar.getInstance()
        val timestamp = Calendar.getInstance().also { it.timeInMillis = pdc.get(KEY_DAILY_TIMESTAMP, PersistentDataType.LONG) ?: System.currentTimeMillis() }

        if (newDataContainer || ! (timestamp.get(Calendar.YEAR) == now.get(Calendar.YEAR) && timestamp.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR))) {
            pdc.set(KEY_DAILY_TIMESTAMP, PersistentDataType.LONG, System.currentTimeMillis())
            pdc.set(KEY_DAILY_TYPES, PersistentDataType.LIST.strings(), questTypes.filter { it.category == QuestCategories.DAILY }
                .shuffled()
                .take(3)
                .map { type -> type.key.asString() })

            persistentDataContainer.set(KEY_DAILY, PersistentDataType.TAG_CONTAINER, pdc)
        }

        return (pdc.get(KEY_DAILY_TYPES, PersistentDataType.LIST.strings()) ?: listOf()).mapNotNull {
            QuestTypes.get(Key.key(it))
        }.toSet()
    }

fun Player.hasPermission(type: net.azisaba.quem.QuestType): Boolean {
    val plays = questTypeMap[type]
    val maxPlays = type.maxPlays
    return plays != null && (maxPlays == null || plays < maxPlays)
}