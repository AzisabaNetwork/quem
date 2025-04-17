package net.azisaba.quem.gui

import com.tksimeji.kunectron.ChestGui
import com.tksimeji.kunectron.hooks.ChestGuiHooks
import net.azisaba.quem.Party
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

abstract class PartyGui(player: Player, val party: Party): ChestGuiHooks, UpdatableGui {
    @ChestGui.Title
    private val title = Component.translatable("gui.party")
}