package net.azisaba.quem

import net.azisaba.quem.impl.QuestImpl
import net.azisaba.quem.impl.SoloPartyImpl
import net.azisaba.quem.gui.QuestPanelUI
import org.bukkit.entity.Player

interface Quest {
    companion object {
        private val instances = mutableSetOf<Quest>()

        val all: Set<Quest>
            get() = instances.toSet()

        fun create(type: QuestType, party: Party): Quest {
            return QuestImpl(type, party).also { instances.add(it) }
        }
    }

    val type: QuestType

    val party: Party

    val progresses: Progresses

    val players: Set<Player>

    val guide: Guide?

    val panel: QuestPanelUI

    fun removePlayer(player: Player)

    fun isPlayer(player: Player?): Boolean

    fun isNotPlayer(player: Player?): Boolean {
        return ! isPlayer(player)
    }

    fun end(reason: EndReason) {
        party.quest = null
        instances.remove(this)

        if (party is SoloPartyImpl) {
            party.disband()
        }
    }

    enum class EndReason {
        CANCEL,
        COMPLETE,
        OTHER,
        PLUGIN,
        RELOAD
    }
}