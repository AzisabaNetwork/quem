package net.azisaba.quem.gui

import com.tksimeji.visualkit.api.Element
import com.tksimeji.visualkit.api.Size
import com.tksimeji.visualkit.element.VisualkitElement
import net.azisaba.quem.Party
import net.azisaba.quem.Quem
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import kotlin.math.max
import kotlin.math.min

class PartyInviteUI(player: Player, party: Party, private val page: Int = 0, private val query: String = "") : PartyUI(player, party), Searchable {
    companion object {
        private val playerSlots = listOf(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34)
    }

    private val players = _players

    private val _players: List<Player>
        get() = Bukkit.getOnlinePlayers().filter { party.isNotMember(it) && it.name.contains(query, ignoreCase = true) }

    @Element(45)
    private val previous = VisualkitElement.create(Material.ARROW)
        .title(Component.translatable("gui.previous").color(NamedTextColor.GREEN))
        .handler { -> PartyInviteUI(player, party, max(page - 1, 0)) }

    @Element(48)
    private val back = VisualkitElement.create(Material.ARROW)
        .title(Component.translatable("gui.back").color(NamedTextColor.GREEN))
        .handler { -> PartyMenuUI(player, party) }

    @Element(49)
    private val exit = VisualkitElement.create(Material.BARRIER)
        .title(Component.translatable("gui.exit").color(NamedTextColor.RED))
        .handler(this::close)

    @Element(50)
    private val search = VisualkitElement.create(Material.COMPASS)
        .title(Component.text("gui.search").color(NamedTextColor.GREEN))
        .handler { -> SearchUI(player, this) }

    @Element(53)
    private val next = VisualkitElement.create(Material.ARROW)
        .title(Component.translatable("gui.next").color(NamedTextColor.GREEN))
        .handler { -> PartyInviteUI(player, party, min(page + 1, _players.size / playerSlots.size)) }

    init {
        for ((index, p) in players.subList(page * playerSlots.size, min((page + 1) * playerSlots.size, players.size)).withIndex()) {
            val slot = playerSlots[index]
            setElement(slot, VisualkitElement.head(p)
                .title(Component.text(p.name))
                .handler { ->
                    if (party.isMember(p)) {
                        PartyInviteUI(player, party, page)
                        return@handler
                    }

                    Bukkit.dispatchCommand(player, "${Quem.PLUGIN_ID}:party invite ${p.name}")
                })
        }
    }

    override fun title(): Component {
        return Component.translatable("gui.party")
    }

    override fun size(): Size {
        return Size.SIZE_54
    }

    override fun search(query: String) {
        PartyInviteUI(player, party, query = query)
    }

    override fun update() {
        PartyInviteUI(player, party, page)
    }
}