package net.azisaba.quem.gui

import com.tksimeji.visualkit.SharedPanelUI
import net.azisaba.quem.Quest
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

class QuestPanelUI(private val quest: Quest): SharedPanelUI() {
    private var progress = 0
    private var partySize = 0

    private val members = quest.party.members.toList()

    init {
        title = Component.text("Reincarnation").color(NamedTextColor.YELLOW)

        setLine(1, Component.text("進行中: ").color(NamedTextColor.GRAY)
            .append(quest.type.name.colorIfAbsent(NamedTextColor.WHITE)))
        setLine(2, Component.text("進捗: ").color(NamedTextColor.GRAY)
            .append(Component.text("\${progress}").color(NamedTextColor.GREEN))
            .append(Component.text("/").color(NamedTextColor.DARK_GRAY))
            .append(Component.text(quest.requirement).color(NamedTextColor.WHITE)))
        setLine(4, Component.text("パーティー (\${partySize}):").color(NamedTextColor.GRAY))
        setLine(5 + 1 + members.size, Component.text("いますぐ ").color(NamedTextColor.GRAY)
            .append(Component.text("azisaba.net").color(NamedTextColor.YELLOW))
            .append(Component.text(" で遊べ！").color(NamedTextColor.GRAY)))
    }

    override fun onTick() {
        super.onTick()
        progress = quest.progress
        partySize = quest.party.members.size

        for ((index, member) in members.withIndex()) {
            val online = quest.party.isMember(member)

            setLine(5 + index, Component.space()
                .append(Component.text(member.name).color(if (online) NamedTextColor.AQUA else NamedTextColor.RED))
                .appendSpace()
                .append(if (online) Component.text(member.health.toInt()).color(NamedTextColor.GREEN).append(Component.text("♥").color(NamedTextColor.RED)) else Component.text("切断").color(NamedTextColor.RED).decorate(TextDecoration.BOLD)))
        }
    }
}