package net.azisaba.quem.impl

import net.azisaba.quem.*
import net.azisaba.quem.gui.QuestPanelUI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

class QuestImpl(override val type: QuestType, override val party: Party) : Quest {
    override val requirement: Int
        get() = progresses.sumOf { it.value }

    override val progress: Int
        get() = progresses.sumOf { it.key.amount }

    override val progresses = Progresses(this)

    override val panel: QuestPanelUI = QuestPanelUI(this)
    init {
        party.quest = this
        party.members.forEach { panel.addAudience(it) }
    }

    override fun onProgressChanged() {
    }

    override fun onEnd(reason: Quest.EndReason) {
        super.onEnd(reason)

        panel.kill()

        if (reason == Quest.EndReason.COMPLETE) {
            party.forEach { it.sendMessage(Component.text("Clear!").color(NamedTextColor.AQUA)) }
        }
    }
}