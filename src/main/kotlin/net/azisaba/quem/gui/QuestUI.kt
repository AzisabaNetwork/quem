package net.azisaba.quem.gui

import com.tksimeji.visualkit.ChestUI
import com.tksimeji.visualkit.api.Element
import com.tksimeji.visualkit.api.Size
import com.tksimeji.visualkit.element.VisualkitElement
import net.azisaba.quem.Party
import net.azisaba.quem.Party.Companion.party
import net.azisaba.quem.Quest
import net.azisaba.quem.QuestCategory
import net.azisaba.quem.registry.QuestCategories
import net.azisaba.quem.registry.QuestTypes
import net.azisaba.quem.extension.hasPermission
import net.azisaba.quem.extension.plainText
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import kotlin.math.max
import kotlin.math.min

class QuestUI(player: Player, page: Int = 0, private val category: QuestCategory? = null, private val query: String = "") : ChestUI(player), Searchable {
    companion object {
        private val categorySlots = listOf(1, 2, 3, 4, 5, 6, 7)
        private val questTypeSlots = listOf(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43)
    }

    private val chunkedCategories
        get() = (listOf(null) + QuestCategories.entries.toList()).chunked(categorySlots.size)

    private val currentCategories = chunkedCategories.firstOrNull { it.contains(category) } ?: throw IllegalStateException()

    private val questTypes = QuestTypes.entries
        .filter { (category == null || it.category == category) && it.title.plainText.contains(query, ignoreCase = true) }
        .toList()

    @Element(9)
    private val categoryPrevious = VisualkitElement.head("http://textures.minecraft.net/texture/76ebaa41d1d405eb6b60845bb9ac724af70e85eac8a96a5544b9e23ad6c96c62")
        .title(Component.translatable("gui.previous").color(NamedTextColor.GREEN))
        .handler { ->
            val currentPartition = chunkedCategories.indexOfFirst { it.contains(category) }.takeIf { 0 < it } ?: return@handler
            val previousPartition = chunkedCategories[currentPartition - 1]
            QuestUI(player, category = previousPartition.first())
        }

    @Element(17)
    private val categoryNext = VisualkitElement.head("http://textures.minecraft.net/texture/8399e5da82ef7765fd5e472f3147ed118d981887730ea7bb80d7a1bed98d5ba")
        .title(Component.translatable("gui.next").color(NamedTextColor.GREEN))
        .handler { ->
            val currentPartition = chunkedCategories.indexOfFirst { it.contains(category) }.takeIf { it < chunkedCategories.size - 1 } ?: return@handler
            val previousPartition = chunkedCategories[currentPartition + 1]
            QuestUI(player, category = previousPartition.first())
        }

    @Element(45)
    private val previous = VisualkitElement.create(Material.ARROW)
        .title(Component.translatable("gui.previous").color(NamedTextColor.GREEN))
        .handler { -> QuestUI(player, max(page - 1, 0), category, query) }

    @Element(49)
    private val exit = VisualkitElement.create(Material.BARRIER)
        .title(Component.translatable("gui.exit").color(NamedTextColor.RED))
        .handler(this::close)

    @Element(50)
    private val search = VisualkitElement.create(Material.COMPASS)
        .title(Component.translatable("gui.search").color(NamedTextColor.GREEN))
        .handler { -> SearchUI(player, this) }

    @Element(53)
    private val next = VisualkitElement.create(Material.ARROW)
        .title(Component.translatable("gui.next").color(NamedTextColor.GREEN))
        .handler { -> QuestUI(player, min(page + 1, questTypes.size / questTypeSlots.size), category, query) }

    init {
        for ((index, category) in currentCategories.withIndex()) {
            val slot = categorySlots[index]

            if (category != null) {
                setElement(slot, VisualkitElement.item(category.icon)
                    .handler { ->
                        QuestUI(player, category = category)
                    })
            } else {
                setElement(slot, VisualkitElement.create(Material.NETHER_STAR)
                    .title(Component.translatable("gui.quest.all"))
                    .handler { ->
                        QuestUI(player, category = null)
                    })
            }

            setElement(slot + 9, VisualkitElement.create(if (category == this.category) Material.GREEN_STAINED_GLASS_PANE else Material.GRAY_STAINED_GLASS_PANE))
        }

        for (slot in questTypeSlots) {
            setElement(slot, VisualkitElement.create(Material.GRAY_DYE))
        }

        for ((index, questType) in questTypes.subList(page * questTypeSlots.size, min((page + 1) * questTypeSlots.size, questTypes.size)).withIndex()) {
            val slot = questTypeSlots[index]
            setElement(slot, VisualkitElement.item(questType.icon)
                .handler { ->
                    val party = player.party ?: Party.solo(player)

                    if (party.leader != player) {
                        player.playSound(player, Sound.ENTITY_PLAYER_TELEPORT, 1.0f, 0.1f)
                        return@handler
                    }

                    if (! party.hasPermission(questType)) {
                        player.playSound(player, Sound.ENTITY_PLAYER_TELEPORT, 1.0f, 0.1f)
                        return@handler
                    }

                    if (party.any { ! it.hasPermission(questType) }) {
                        player.playSound(player, Sound.ENTITY_PLAYER_TELEPORT, 1.0f, 0.1f)
                        return@handler
                    }

                    Quest.create(questType, party)
                    close()
                })
        }
    }

    override fun title(): Component {
        return Component.translatable("gui.quest")
    }

    override fun size(): Size {
        return Size.SIZE_54
    }

    override fun search(query: String) {
        QuestUI(player, query = query)
    }
}