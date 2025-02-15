package net.azisaba.quem.gui

import com.tksimeji.visualkit.SharedPanelUI
import net.azisaba.quem.Quem
import net.azisaba.quem.Quest
import net.azisaba.quem.Stage
import net.azisaba.quem.util.toTextComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

class QuestPanelUI(private val quest: Quest): SharedPanelUI() {
    private var progress = 0
    private var partySize = 0

    private val party = quest.party

    private val members = quest.players.toList()

    private var staged = false

    init {
        title = Quem.pluginConfig.panel.title.toTextComponent()
        init()
    }

    private fun init() {
        clear()

        setLine(1, Component.text("進行中: ").color(NamedTextColor.GRAY)
            .append(quest.type.title.colorIfAbsent(NamedTextColor.WHITE)))
        setLine(2, Component.text("進捗: ").color(NamedTextColor.GRAY)
            .append(Component.text("\${progress}").color(NamedTextColor.GREEN))
            .append(Component.text("/").color(NamedTextColor.DARK_GRAY))
            .append(Component.text(quest.progresses.sumOf { it.key.amount }).color(NamedTextColor.WHITE)))
        setLine(4, Component.text("パーティー (\${partySize}):").color(NamedTextColor.GRAY))
    }

    override fun onTick() {
        super.onTick()
        progress = quest.progresses.sumOf { it.value }
        partySize = quest.party.members.size

        for ((index, member) in members.withIndex()) {
            val isPlayer = quest.isPlayer(member)

            val component = Component.space()
                .append(Component.text(member.name)
                    .color(if (isPlayer) NamedTextColor.AQUA else NamedTextColor.RED)
                    .decoration(TextDecoration.STRIKETHROUGH, if (isPlayer) TextDecoration.State.NOT_SET else TextDecoration.State.TRUE))
                .appendSpace()
                .append(when {
                    isPlayer -> {
                        val health = member.health.toInt()
                        val healthColor = when {
                            health <= 4 -> NamedTextColor.RED
                            health <= 8 -> NamedTextColor.GOLD
                            else -> NamedTextColor.GREEN
                        }

                        Component.text(health).color(healthColor).append(Component.text("♥").color(NamedTextColor.RED))
                    }
                    party.isNotMember(member) -> Component.text("離脱").color(NamedTextColor.RED).decorate(TextDecoration.BOLD)
                    else -> Component.text("脱落").color(NamedTextColor.RED).decorate(TextDecoration.BOLD)
                })

            setLine(5 + index, component)
        }

        var index = 5 + partySize + 1

        if (staged && ! party.hasStage()) {
            init()
        }

        if (party.hasStage()) {
            val stage = party.stage!!

            if (stage is Stage) {
                setLine(index, Component.text("ステージ: ").color(NamedTextColor.GRAY).append(stage.title.colorIfAbsent(NamedTextColor.WHITE)))
            } else if (stage is Stage.Queue) {
                setLine(index, Component.text("ステージキュー: ").color(NamedTextColor.GRAY)
                    .append(Component.text(stage.indexOf(party).plus(1).toString()).color(NamedTextColor.GREEN))
                    .append(Component.text("/").color(NamedTextColor.DARK_GRAY))
                    .append(Component.text(stage.size).color(NamedTextColor.WHITE)))
            }

            staged = true
            index += 2
        } else {
            staged = false
        }

        setLine(index, Quem.pluginConfig.panel.footer.toTextComponent())
    }
}