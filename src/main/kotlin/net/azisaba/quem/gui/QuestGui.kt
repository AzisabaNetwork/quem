package net.azisaba.quem.gui

import com.tksimeji.kunectron.ChestGui
import com.tksimeji.kunectron.Kunectron
import com.tksimeji.kunectron.element.Element
import com.tksimeji.kunectron.event.ChestGuiEvents
import com.tksimeji.kunectron.event.GuiHandler
import com.tksimeji.kunectron.hooks.ChestGuiHooks
import net.azisaba.quem.*
import net.azisaba.quem.Party.Companion.party
import net.azisaba.quem.extension.plainText
import net.azisaba.quem.registry.QuestCategories
import net.azisaba.quem.registry.QuestTypes
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemType
import java.net.URI
import kotlin.math.max
import kotlin.math.min

@ChestGui
class QuestGui(@ChestGui.Player private val player: Player, private val page: Int = 0, private val category: QuestCategory? = null, private val query: String = "") : ChestGuiHooks, SearchableGui {
    private val currentChunk = CHUNKED_CATEGORIES.indexOfFirst { it.contains(category) }

    private val currentCategories = CHUNKED_CATEGORIES.firstOrNull { it.contains(category) } ?: throw IllegalStateException()

    private val questTypes = QuestTypes.entries
        .filter { (category == null || it.category == category) &&
                it.title.plainText.contains(query, ignoreCase = true) &&
                (category != QuestCategories.DAILY || player.dailyQuestTypes.contains(it)) }
        .toList()

    @ChestGui.Title
    private val title = Component.translatable("gui.quest")

    @ChestGui.Element(index = [9])
    private val previousTab = Element.playerHead(URI.create("http://textures.minecraft.net/texture/76ebaa41d1d405eb6b60845bb9ac724af70e85eac8a96a5544b9e23ad6c96c62").toURL())
        .title(Component.translatable("gui.previous").color(NamedTextColor.GREEN))
        .handler { ->
            if (currentChunk <= 0) return@handler
            val previousChunk = CHUNKED_CATEGORIES[currentChunk - 1]
            Kunectron.create(QuestGui(player, category = previousChunk[currentChunk - 1]))
        }

    @ChestGui.Element(index = [17])
    private val nextTab = Element.playerHead(URI.create("http://textures.minecraft.net/texture/8399e5da82ef7765fd5e472f3147ed118d981887730ea7bb80d7a1bed98d5ba").toURL())
        .title(Component.translatable("gui.next").color(NamedTextColor.GREEN))
        .handler { ->
            if (currentChunk >= CHUNKED_CATEGORIES.size - 1) return@handler
            val nextChunk = CHUNKED_CATEGORIES[currentChunk + 1]
            Kunectron.create(QuestGui(player, category = nextChunk.first()))
        }

    @ChestGui.Element(index = [45])
    private val previous = Element.item(ItemType.ARROW)
        .title(Component.translatable("gui.previous").color(NamedTextColor.GREEN))
        .handler { -> Kunectron.create(QuestGui(player, max(page - 1, 0), category, query)) }

    @ChestGui.Element(index = [49])
    private val close = Element.item(ItemType.BARRIER)
        .title(Component.translatable("gui.close").color(NamedTextColor.RED))
        .handler(this::useClose)

    @ChestGui.Element(index = [50])
    private val search = Element.item(ItemType.COMPASS)
        .title(Component.translatable("gui.search").color(NamedTextColor.GREEN))
        .handler { -> Kunectron.create(SearchGui(player, this)) }

    @ChestGui.Element(index = [53])
    private val next = Element.item(ItemType.ARROW)
        .title(Component.translatable("gui.next").color(NamedTextColor.GREEN))
        .handler { -> Kunectron.create(QuestGui(player, min(page + 1, questTypes.size / QUEST_TYPE_INDEXES.size), category, query)) }

    override fun search(query: String) {
        Kunectron.create(QuestGui(player, query = query))
    }

    @GuiHandler
    private fun onInit(event: ChestGuiEvents.InitEvent) {
        for ((index, category) in currentCategories.withIndex()) {
            val categoryIndex = CATEGORY_INDEXES[index]
            if (category != null) {
                useElement(categoryIndex, Element.item(category.icon).handler { -> Kunectron.create(QuestGui(player, category = category)) })
            } else {
                useElement(categoryIndex, Element.item(ItemType.NETHER_STAR)
                    .title(Component.translatable("gui.quest.all"))
                    .handler { -> Kunectron.create(QuestGui(player, category = null)) })
            }
            useElement(categoryIndex + 9, Element.item(if (category == this.category) ItemType.GREEN_STAINED_GLASS_PANE else ItemType.GRAY_STAINED_GLASS_PANE))
        }

        for (questTypeIndex in QUEST_TYPE_INDEXES) {
            useElement(questTypeIndex, Element.item(ItemType.GRAY_DYE))
        }

        for ((index, questType) in questTypes.subList(page * QUEST_TYPE_INDEXES.size, min((page + 1) * QUEST_TYPE_INDEXES.size, questTypes.size)).withIndex()) {
            val questTypeIndex = QUEST_TYPE_INDEXES[index]
            useElement(questTypeIndex, Element.item(questType.icon)
                .lore(*questType.description.map { it.colorIfAbsent(NamedTextColor.GRAY) }.toTypedArray(),
                    Component.empty(),
                    Component.text("-".repeat(14)).color(NamedTextColor.DARK_GRAY),
                    let {
                        if (questType.hasPlayLimit()) {
                            val plays = player.questTypeMap[questType] ?: 0

                            Component.text("プレイ回数: ").color(NamedTextColor.GRAY)
                                .append(Component.text(plays).color(if (plays == questType.maxPlays) NamedTextColor.RED else NamedTextColor.YELLOW))
                                .append(Component.text("/").color(NamedTextColor.DARK_GRAY))
                                .append(Component.text(questType.maxPlays!!).color(NamedTextColor.WHITE))
                        } else {
                            Component.text("プレイ上限なし").color(NamedTextColor.GRAY)
                        }
                    })
                .handler { ->
                    val party = player.party ?: Party.solo(player)

                    if (party.leader != player) {
                        player.sendMessage(Component.translatable("gui.quest.permissionError").color(NamedTextColor.RED))
                        player.playSound(player, Sound.ENTITY_PLAYER_TELEPORT, 1.0f, 0.1f)
                        return@handler
                    }

                    if (party.hasQuest()) {
                        player.sendMessage(Component.translatable("gui.quest.already").color(NamedTextColor.RED))
                        player.playSound(player, Sound.ENTITY_PLAYER_TELEPORT, 1.0f, 0.1f)
                        return@handler
                    }

                    if (! party.hasPermission(questType)) {
                        player.sendMessage(Component.translatable("gui.quest.sizeError").color(NamedTextColor.RED))
                        player.playSound(player, Sound.ENTITY_PLAYER_TELEPORT, 1.0f, 0.1f)
                        return@handler
                    }

                    if (party.any { ! it.hasPermission(questType) }) {
                        player.sendMessage(Component.translatable("gui.quest.memberError").color(NamedTextColor.RED))
                        player.playSound(player, Sound.ENTITY_PLAYER_TELEPORT, 1.0f, 0.1f)
                        return@handler
                    }

                    Quest.create(questType, party)
                })
        }
    }

    companion object {
        private val CATEGORY_INDEXES = listOf(1, 2, 3, 4, 5, 6, 7)
        private val QUEST_TYPE_INDEXES = listOf(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43)

        private val CHUNKED_CATEGORIES
            get() = (listOf(null) + QuestCategories.entries.toList()).chunked(CATEGORY_INDEXES.size)
    }
}