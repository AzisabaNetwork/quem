package net.azisaba.quem.gui

import com.tksimeji.visualkit.ChestUI
import com.tksimeji.visualkit.api.Element
import com.tksimeji.visualkit.api.Size
import com.tksimeji.visualkit.element.VisualkitElement
import net.azisaba.quem.Party
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player

class PartyCreateUI(player: Player) : ChestUI(player) {
    @Element(22)
    private val create = VisualkitElement.create(Material.CRAFTING_TABLE)
        .title(Component.translatable("gui.partyCreate.create").color(NamedTextColor.GREEN))
        .lore(Component.translatable("gui.partyCreate.create.description").color(NamedTextColor.GRAY))
        .handler { ->
            PartyMenuUI(player, Party.create(player))
        }

    @Element(40)
    private val exit = VisualkitElement.create(Material.BARRIER)
        .title(Component.translatable("gui.exit").color(NamedTextColor.RED))
        .handler(this::close)

    override fun title(): Component {
        return Component.translatable("gui.party")
    }

    override fun size(): Size {
        return Size.SIZE_54
    }
}