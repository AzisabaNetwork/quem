package net.azisaba.quem

import net.azisaba.quem.impl.QuestImpl
import net.azisaba.quem.impl.SoloPartyImpl
import net.azisaba.quem.gui.QuestPanelUI

interface Quest {
    companion object {
        private val instances = mutableSetOf<Quest>()

        fun create(type: QuestType, party: Party): Quest {
            return QuestImpl(type, party).also { instances.add(it) }
        }
    }

    val type: QuestType

    val party: Party

    val requirement: Int

    val progress: Int

    val progresses: Progresses

    val panel: QuestPanelUI

    fun onProgressChanged()

    fun onEnd(reason: EndReason) {
        party.quest = null
        instances.remove(this)

        if (party is SoloPartyImpl) {
            party.disband()
        }
    }

    enum class EndReason {
        CANCEL,
        COMPLETE,
        PLUGIN
    }
}