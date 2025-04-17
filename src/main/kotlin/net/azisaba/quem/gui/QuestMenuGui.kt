package net.azisaba.quem.gui

import com.tksimeji.kunectron.ChestGui
import com.tksimeji.kunectron.Kunectron
import com.tksimeji.kunectron.element.Element
import com.tksimeji.kunectron.hooks.ChestGuiHooks
import net.azisaba.quem.Quest
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemType

@ChestGui
class QuestMenuGui(@ChestGui.Player private val player: Player, private val quest: Quest): ChestGuiHooks {
    @ChestGui.Title
    private val title = Component.translatable("gui.questMenu")

    @ChestGui.Size
    private val size = ChestGui.ChestSize.SIZE_27

    @ChestGui.Element(index = [11])
    private val questInfo = quest.type.createItemElement(player)

    @ChestGui.Element(index = [13])
    private val quit = Element.item(ItemType.TNT_MINECART)
        .title(Component.translatable(if (player != quest.party.leader) "gui.partyMenu.quit" else "gui.partyMenu.disband").color(NamedTextColor.RED).decorate(
            TextDecoration.BOLD))
        .handler { -> Kunectron.create(ConfirmGui(player, { quest.party.removeMember(player) }, null)) }

    @ChestGui.Element(index = [15])
    private val close = Element.item(ItemType.BARRIER)
        .title(Component.translatable("gui.close").color(NamedTextColor.RED))
        .handler(this::useClose)
}