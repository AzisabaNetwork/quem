package net.azisaba.quem.gui

import com.tksimeji.kunectron.ChestGui
import com.tksimeji.kunectron.Kunectron
import com.tksimeji.kunectron.element.Element
import com.tksimeji.kunectron.hooks.ChestGuiHooks
import net.azisaba.quem.Party
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemType

@ChestGui
class PartyCreateGui(@ChestGui.Player private val player: Player): ChestGuiHooks {
    @ChestGui.Title
    private val title = Component.translatable("gui.party")

    @ChestGui.Element(index = [22])
    private val create = Element.item(ItemType.CRAFTING_TABLE)
        .title(Component.translatable("gui.partyCreate.create").color(NamedTextColor.GREEN))
        .lore(Component.translatable("gui.partyCreate.create.description").color(NamedTextColor.GRAY))
        .handler { -> Kunectron.create(PartyMenuGui(player, Party.create(player))) }

    @ChestGui.Element(index = [40])
    private val close = Element.item(ItemType.BARRIER)
        .title(Component.translatable("gui.close").color(NamedTextColor.RED))
        .handler(this::useClose)
}