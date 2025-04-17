package net.azisaba.quem.gui

import com.tksimeji.kunectron.ScoreboardGui
import com.tksimeji.kunectron.event.GuiHandler
import com.tksimeji.kunectron.event.ScoreboardGuiEvents
import com.tksimeji.kunectron.hooks.ScoreboardGuiHooks
import net.azisaba.quem.Quem
import net.azisaba.quem.Quest
import net.azisaba.quem.Stage
import net.azisaba.quem.extension.toTextComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

@ScoreboardGui
class QuestPanelGui(private val quest: Quest): ScoreboardGuiHooks {
    private val progress
        get() = quest.progresses.sumOf { it.value }

    private val party = quest.party

    private val members = quest.players.toList()

    private var staged = false

    @ScoreboardGui.Title
    private val title = Quem.pluginConfig.panel.title.toTextComponent()

    @ScoreboardGui.Line(index = 1)
    private val line2 = Component.text("進行中: ").color(NamedTextColor.GRAY).append(quest.type.title.colorIfAbsent(NamedTextColor.WHITE))

    @ScoreboardGui.Line(index = 2)
    private val line3 = Component.text("進捗: ").color(NamedTextColor.GRAY)
        .append(Component.text("{ getProgress() }").color(NamedTextColor.GREEN))
        .append(Component.text("/").color(NamedTextColor.DARK_GRAY))
        .append(Component.text(quest.progresses.sumOf { it.key.amount }).color(NamedTextColor.WHITE))

    @ScoreboardGui.Line(index = 4)
    private val line5 = Component.text("パーティー ({ quest.getParty().getMembers().size() }):").color(NamedTextColor.GRAY)

    @GuiHandler
    fun onTick(event: ScoreboardGuiEvents.TickEvent) {
        var index = 5

        for (member in members) {
            val isPlayer = quest.isPlayer(member)
            val component = Component.space()
                .append(Component.text(member.name).color(if (isPlayer) NamedTextColor.AQUA else NamedTextColor.RED).decoration(TextDecoration.STRIKETHROUGH, if (isPlayer) TextDecoration.State.NOT_SET else TextDecoration.State.TRUE))
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
            useLine(index++, component)
        }

        index++

        if (staged && !party.hasStage()) {
            useRemoveLines()
            useLine(1, line2)
            useLine(2, line3)
            useLine(4, line5)
        }

        if (party.hasStage()) {
            val stage = party.stage!!

            if (stage is Stage) {
                useLine(index, Component.text("ステージ: ").color(NamedTextColor.GRAY).append(stage.title.colorIfAbsent(NamedTextColor.WHITE)))
            } else if (stage is Stage.Queue) {
                useLine(index, Component.text("ステージキュー: ").color(NamedTextColor.GRAY)
                    .append(Component.text(stage.indexOf(party).plus(1).toString()).color(NamedTextColor.GREEN))
                    .append(Component.text("/").color(NamedTextColor.DARK_GRAY))
                    .append(Component.text(stage.size).color(NamedTextColor.WHITE)))
            }

            staged = true
            index += 2
        } else {
            staged = false
        }

        useLine(index, Quem.pluginConfig.panel.footer.toTextComponent())
    }
}