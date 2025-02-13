package net.azisaba.quem.impl

import net.azisaba.quem.*
import net.azisaba.quem.gui.QuestPanelUI
import net.azisaba.quem.util.hasPermission
import net.azisaba.quem.util.questTypeMap

class QuestImpl(override val type: QuestType, override val party: Party) : Quest {
    override val requirement: Int
        get() = progresses.sumOf { it.value }

    override val progress: Int
        get() = progresses.sumOf { it.key.amount }

    override val progresses = Progresses(this)

    override val panel: QuestPanelUI = QuestPanelUI(this)

    init {
        if (type.maxPlayers != null && type.maxPlayers < party.size) {
            throw IllegalArgumentException("The maximum number of players specified by ${type.key.asString()} has been exceeded: ${party.size}")
        }

        if (party.any { ! it.hasPermission(type) }) {
            throw IllegalArgumentException("The party includes a player who does not have the permission to play")
        }

        party.quest = this
        party.members.forEach { panel.addAudience(it) }
    }

    override fun onProgressChanged() {
    }

    override fun onEnd(reason: Quest.EndReason) {
        super.onEnd(reason)

        panel.kill()

        for (member in party) {
            member.questTypeMap = member.questTypeMap.also {
                it[type] = (it[type] ?: 0) + 1
            }
        }
    }
}