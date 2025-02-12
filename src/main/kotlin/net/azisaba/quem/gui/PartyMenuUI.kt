package net.azisaba.quem.gui

import com.tksimeji.visualkit.api.Action
import com.tksimeji.visualkit.api.Element
import com.tksimeji.visualkit.api.Mouse
import com.tksimeji.visualkit.element.VisualkitElement
import net.azisaba.quem.Party
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.entity.Player
import kotlin.math.max
import kotlin.math.min

class PartyMenuUI(player: Player, party: Party, private val page: Int = 0) : PartyUI(player, party) {
    companion object {
        private val memberSlots = listOf(11, 12, 13, 14, 15, 20, 21, 22, 23, 24, 29, 30, 31, 32, 33)
    }

    private val members = party.members.toList()

    @Element(18)
    private val previous = VisualkitElement.create(Material.ARROW)
        .title(Component.translatable("gui.previous").color(NamedTextColor.GREEN))
        .handler { -> PartyMenuUI(player, party, max(page - 1, 0)) }

    @Element(26)
    private val next = VisualkitElement.create(Material.ARROW)
        .title(Component.translatable("gui.next").color(NamedTextColor.GREEN))
        .handler { -> PartyMenuUI(player, party, min(page + 1, members.size / memberSlots.size)) }

    @Element(49)
    private val exit = VisualkitElement.create(Material.BARRIER)
        .title(Component.translatable("gui.exit").color(NamedTextColor.RED))
        .handler(this::close)

    @Element(50)
    private val quit = VisualkitElement.create(Material.TNT_MINECART)
        .title(Component.translatable(if (player == party.leader) "gui.partyMenu.quit" else "gui.partyMenu.disband").color(NamedTextColor.RED).decorate(TextDecoration.BOLD))
        .handler { -> party.removeMember(player) }

    init {
        var lastIndex = memberSlots.first()

        for ((index, member) in members.subList(page * memberSlots.size, min((page + 1) * memberSlots.size, members.size)).withIndex()) {
            lastIndex = index

            val slot = memberSlots[index]
            val element = VisualkitElement.head(member)
                .title((if (member == party.leader) Component.text("👑").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD).appendSpace() else Component.empty())
                    .append(Component.text(member.name)))

            if (player == party.leader && member != party.leader) {
                element.lore(Component.translatable("gui.partyMenu.member.description").color(NamedTextColor.DARK_GRAY),
                    Component.translatable("gui.partyMenu.member.leftClick").color(NamedTextColor.GRAY),
                    Component.translatable("gui.partyMenu.member.rightClick").color(NamedTextColor.GRAY))
                    .handler { _, action, mouse ->
                        if (action != Action.SHIFT_CLICK) {
                            return@handler
                        }

                        if (mouse == Mouse.LEFT) {
                            party.removeMember(member)
                        } else if (mouse == Mouse.RIGHT) {
                            party.leader = member
                            PartyMenuUI(player, party)
                        }
                    }
            }

            setElement(slot, element)
        }

        for (index in lastIndex + 1 until memberSlots.size) {
            setElement(memberSlots[index], VisualkitElement.create(Material.CLAY_BALL))
        }

        if (party.size < Party.MAX_SIZE && ! party.hasQuest()) {
            setElement(memberSlots[party.size], VisualkitElement.head("http://textures.minecraft.net/texture/dd1500e5b04c8053d40c7968330887d24b073daf1e273faf4db8b62ebd99da83")
                .title(Component.translatable("gui.partyMenu.invite").color(NamedTextColor.GREEN))
                .handler { -> PartyInviteUI(player, party) })
        }
    }

    override fun update() {
        PartyMenuUI(player, party)
    }
}