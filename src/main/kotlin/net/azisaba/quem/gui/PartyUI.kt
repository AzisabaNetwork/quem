package net.azisaba.quem.gui

import com.tksimeji.visualkit.ChestUI
import com.tksimeji.visualkit.api.Size
import net.azisaba.quem.Party
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

abstract class PartyUI(player: Player, val party: Party) : ChestUI(player), Updatable {
    override fun title(): Component {
        return Component.translatable("gui.party")
    }

    override fun size(): Size {
        return Size.SIZE_54
    }
}