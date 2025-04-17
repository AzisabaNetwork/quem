package net.azisaba.quem.gui

import com.tksimeji.kunectron.ChestGui
import com.tksimeji.kunectron.Kunectron
import com.tksimeji.kunectron.element.Element
import com.tksimeji.kunectron.event.ChestGuiEvents
import com.tksimeji.kunectron.event.GuiHandler
import com.tksimeji.kunectron.hooks.ChestGuiHooks
import net.azisaba.quem.Party
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemType
import java.net.URI
import kotlin.math.max
import kotlin.math.min

@ChestGui
class PartyMenuGui(@ChestGui.Player private val player: Player, party: Party, private val page: Int = 0): PartyGui(player, party), ChestGuiHooks {
    private val members = party.members.toList()

    @ChestGui.Element(index = [18])
    private val previous = Element.item(ItemType.ARROW)
        .title(Component.translatable("gui.previous").color(NamedTextColor.GREEN))
        .handler { -> Kunectron.create(PartyMenuGui(player, party, max(page - 1, 0))) }

    @ChestGui.Element(index = [26])
    private val next = Element.item(ItemType.ARROW)
        .title(Component.translatable("gui.next").color(NamedTextColor.GREEN))
        .handler { -> Kunectron.create(PartyMenuGui(player, party, min(page + 1, members.size / MEMBER_INDEXES.size))) }

    @ChestGui.Element(index = [47])
    private val invitationSetting = Element.item(ItemType.COMPARATOR)
        .title(Component.translatable("gui.partyMenu.invitationSetting").color(NamedTextColor.GREEN))
        .lore(Component.translatable("gui.partyMenu.invitationSetting.description").color(NamedTextColor.GRAY),
            Component.empty(),
            Component.translatable("gui.partyMenu.invitationSetting.leader").color(if (party.invitationSetting == Party.InvitationSetting.LEADER) NamedTextColor.GREEN else NamedTextColor.DARK_GRAY),
            Component.translatable("gui.partyMenu.invitationSetting.all").color(if (party.invitationSetting == Party.InvitationSetting.ALL) NamedTextColor.GREEN else NamedTextColor.DARK_GRAY))
        .handler { ->
            if (player != party.leader) {
                return@handler
            }

            party.invitationSetting = when (party.invitationSetting) {
                Party.InvitationSetting.LEADER -> Party.InvitationSetting.ALL
                Party.InvitationSetting.ALL -> Party.InvitationSetting.LEADER
            }
        }

    @ChestGui.Element(index = [48])
    private val quest = Element.item(ItemType.ENCHANTING_TABLE)
        .title(Component.translatable("gui.partyMenu.quest").color(NamedTextColor.LIGHT_PURPLE))
        .lore(if (!party.hasQuest()) Component.translatable("gui.partyMenu.quest.empty").color(NamedTextColor.GRAY) else party.quest!!.type.title)
        .handler { ->
            if (!party.hasQuest()) {
                Kunectron.create(QuestGui(player))
            }
        }

    @ChestGui.Element(index = [49])
    private val close = Element.item(ItemType.BARRIER)
        .title(Component.translatable("gui.close").color(NamedTextColor.RED))
        .handler(this::useClose)

    @ChestGui.Element(index = [50])
    private val quit = Element.item(ItemType.TNT_MINECART)
        .title(Component.translatable(if (player != party.leader) "gui.partyMenu.quit" else "gui.partyMenu.disband").color(NamedTextColor.RED).decorate(TextDecoration.BOLD))
        .handler { -> Kunectron.create(ConfirmGui(player, { party.removeMember(player) }, null)) }

    @GuiHandler
    fun onInit(event: ChestGuiEvents.InitEvent) {
        var lastIndex = MEMBER_INDEXES.first()

        for ((index, member) in members.subList(page * MEMBER_INDEXES.size, min((page + 1) * MEMBER_INDEXES.size, members.size)).withIndex()) {
            lastIndex = index
            val memberIndex = MEMBER_INDEXES[index]
            val element = Element.playerHead(member)
                .title((if (member == party.leader) Component.text("👑").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD).appendSpace() else Component.empty())
                    .append(Component.text(member.name)))

            if (player == party.leader && member != party.leader) {
                element.lore(Component.translatable("gui.partyMenu.member.description").color(NamedTextColor.DARK_GRAY),
                    Component.translatable("gui.partyMenu.member.leftClick").color(NamedTextColor.GRAY),
                    Component.translatable("gui.partyMenu.member.rightClick").color(NamedTextColor.GRAY))
                    .handler { event ->
                        if (!event.isShiftClick) {
                            return@handler
                        }
                        if (event.isLeftClick) {
                            party.removeMember(member)
                        } else if (event.isRightClick) {
                            party.leader = member
                            Kunectron.create(PartyMenuGui(player, party))
                        }
                    }
            }

            useElement(memberIndex, element)
        }

        for (index in lastIndex + 1 until MEMBER_INDEXES.size) {
            useElement(MEMBER_INDEXES[index], Element.item(ItemType.CLAY_BALL))
        }

        if (party.size < Party.MAX_SIZE && !party.hasQuest() && party.hasInvitationPermission(player)) {
            useElement(MEMBER_INDEXES[party.size], Element.playerHead(URI.create("http://textures.minecraft.net/texture/dd1500e5b04c8053d40c7968330887d24b073daf1e273faf4db8b62ebd99da83").toURL())
                .title(Component.translatable("gui.partyMenu.invite").color(NamedTextColor.GREEN))
                .handler { -> Kunectron.create(PartyInviteGui(player, party)) })
        }

        if (party.hasQuest() && player == party.leader) {
            useElement(51, Element.item(ItemType.REDSTONE_TORCH))
        }
    }

    override fun update() {
        Kunectron.create(PartyMenuGui(player, party))
    }

    companion object {
        private val MEMBER_INDEXES = listOf(11, 12, 13, 14, 15, 20, 21, 22, 23, 24, 29, 30, 31, 32, 33)
    }
}