package net.azisaba.quem.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import net.azisaba.quem.Party
import net.azisaba.quem.Party.Companion.party
import net.azisaba.quem.Quest
import net.azisaba.quem.QuestType
import net.azisaba.quem.gui.PartyCreateUI
import net.azisaba.quem.gui.PartyMenuUI
import net.azisaba.quem.gui.QuestUI
import net.azisaba.quem.util.CommandSyntaxException
import org.bukkit.entity.Player

object QuemCommand {
    fun create(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("quem")
            .then(Commands.literal("party")
                .executes { ctx ->
                    val player = (ctx.source.executor.takeIf { it is Player } ?: return@executes Command.SINGLE_SUCCESS) as Player

                    if (player.party == null) {
                        PartyCreateUI(player)
                    } else {
                        PartyMenuUI(player, player.party!!)
                    }

                    Command.SINGLE_SUCCESS
                })
            .then(Commands.literal("quests")
                .executes { ctx ->
                    val player = (ctx.source.executor.takeIf { it is Player } ?: return@executes Command.SINGLE_SUCCESS) as Player
                    QuestUI(player)
                    Command.SINGLE_SUCCESS
                })
            .then(Commands.literal("progress")
                .then(Commands.argument("target", ArgumentTypes.player())
                    .then(Commands.argument("requirement", StringArgumentType.word())
                        .then(Commands.argument("formula", FormulaArgumentType)
                            .executes { ctx -> progressCommand(ctx) }))))
            .then(Commands.literal("start")
                .then(Commands.argument("type", QuestTypeArgumentType)
                    .executes { ctx -> startCommand(ctx) }))
    }

    private fun progressCommand(ctx: CommandContext<CommandSourceStack>): Int {
        val target = ctx.getArgument("target", PlayerSelectorArgumentResolver::class.java).resolve(ctx.source).first()
        val quest = target.party?.quest ?: throw CommandSyntaxException("No quests related to ${target.name} were found")
        val requirement = quest.type.requirements.firstOrNull { it.key == ctx.getArgument("requirement", String::class.java) } ?: throw CommandSyntaxException("Unknown requirement")

        val base = quest.progresses[requirement]
        val formula = ctx.getArgument("formula", FormulaArgumentType.Formula::class.java)

        quest.progresses[requirement] = formula.calculate(base)
        return Command.SINGLE_SUCCESS
    }

    private fun startCommand(ctx: CommandContext<CommandSourceStack>): Int {
        val executor = ctx.source.executor

        if (executor !is Player) {
            return Command.SINGLE_SUCCESS
        }

        val type = ctx.getArgument("type", QuestType::class.java)
        Quest.create(type, executor.party ?: Party.solo(executor))
        return Command.SINGLE_SUCCESS
    }
}