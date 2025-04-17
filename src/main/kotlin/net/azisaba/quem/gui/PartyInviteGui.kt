package net.azisaba.quem.gui

import com.tksimeji.kunectron.ChestGui
import com.tksimeji.kunectron.Kunectron
import com.tksimeji.kunectron.element.Element
import com.tksimeji.kunectron.event.ChestGuiEvents
import com.tksimeji.kunectron.event.GuiHandler
import com.tksimeji.kunectron.hooks.ChestGuiHooks
import net.azisaba.quem.Party
import net.azisaba.quem.Quem
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemType
import kotlin.math.max
import kotlin.math.min

@ChestGui
class PartyInviteGui(@ChestGui.Player private val player: Player, party: Party, private val page: Int = 0, private val query: String = ""): PartyGui(player, party), ChestGuiHooks, SearchableGui {
    private val players
        get() = Bukkit.getOnlinePlayers().filter { party.isNotMember(it) && it.name.contains(query, ignoreCase = true) }

    @ChestGui.Title
    private val title = Component.translatable("gui.party")

    @ChestGui.Element(index = [45])
    private val previous = Element.item(ItemType.ARROW)
        .title(Component.translatable("gui.previous").color(NamedTextColor.GREEN))
        .handler { -> Kunectron.create(PartyInviteGui(player, party, max(page - 1, 0))) }

    @ChestGui.Element(index = [48])
    private val back = Element.item(ItemType.ARROW)
        .title(Component.translatable("gui.back").color(NamedTextColor.GREEN))
        .handler { -> Kunectron.create(PartyMenuGui(player, party)) }

    @ChestGui.Element(index = [49])
    private val close = Element.item(ItemType.BARRIER)
        .title(Component.translatable("gui.close").color(NamedTextColor.RED))
        .handler(this::useClose)

    @ChestGui.Element(index = [53])
    private val next = Element.item(ItemType.ARROW)
        .title(Component.translatable("gui.next").color(NamedTextColor.GREEN))
        .handler { -> Kunectron.create(PartyInviteGui(player, party, min(page + 1, players.size / PLAYER_INDEXES.size))) }

    @GuiHandler
    private fun onInit(event: ChestGuiEvents.InitEvent) {
        for ((index, aPlayer) in players.subList(page * PLAYER_INDEXES.size, min((page + 1) * PLAYER_INDEXES.size, players.size)).withIndex()) {
            val playerIndex = PLAYER_INDEXES[index]
            useElement(playerIndex, Element.playerHead(aPlayer).title(Component.text(player.name))
                .handler { ->
                    if (party.isMember(aPlayer)) {
                        Kunectron.create(PartyInviteGui(player, party, page, query))
                        return@handler
                    }
                    Bukkit.dispatchCommand(player, "${Quem.PLUGIN_ID}:party invite ${aPlayer.name}")
                })
        }
    }

    override fun search(query: String) {
        Kunectron.create(PartyInviteGui(player, party, query = query))
    }

    override fun update() {
        Kunectron.create(PartyInviteGui(player, party, page, query))
    }

    companion object {
        private val PLAYER_INDEXES = listOf(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34)
    }
}