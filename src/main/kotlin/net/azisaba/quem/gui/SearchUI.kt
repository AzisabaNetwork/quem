package net.azisaba.quem.gui

import com.tksimeji.visualkit.AnvilUI
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

class SearchUI(player: Player, private val searchable: Searchable) : AnvilUI(player) {
    override fun title(): Component {
        return Component.translatable("gui.search")
    }

    override fun onTyped(string: String) {
        searchable.search(string)
    }
}