package net.azisaba.quem.gui

import com.tksimeji.visualkit.ChestUI
import com.tksimeji.visualkit.api.Element
import com.tksimeji.visualkit.api.Size
import com.tksimeji.visualkit.element.VisualkitElement
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.entity.Player

class ConfirmUI(player: Player, onAccept: Runnable?, onReject: Runnable?) : ChestUI(player) {
    @Element(11)
    private val accept = VisualkitElement.create(Material.GREEN_TERRACOTTA)
        .title(Component.translatable("gui.confirm.accept").color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD))
        .handler { ->
            close()
            onAccept?.run()
        }

    @Element(15)
    private val reject = VisualkitElement.create(Material.RED_TERRACOTTA)
        .title(Component.translatable("gui.confirm.reject").color(NamedTextColor.RED).decorate(TextDecoration.BOLD))
        .handler { ->
            close()
            onReject?.run()
        }

    override fun title(): Component {
        return Component.translatable("gui.confirm")
    }

    override fun size(): Size {
        return Size.SIZE_36
    }
}