package net.azisaba.quem.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import net.azisaba.quem.Party.Companion.party
import net.azisaba.quem.QuemLoader
import net.azisaba.quem.QuestType
import net.azisaba.quem.Stage
import net.azisaba.quem.gui.PartyCreateUI
import net.azisaba.quem.gui.PartyMenuUI
import net.azisaba.quem.gui.QuestUI
import net.azisaba.quem.extension.CommandSyntaxException
import net.azisaba.quem.extension.questTypeMap
import org.bukkit.entity.Player

object QuemCommand {
    fun create(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("quem")
            .then(Commands.literal("debug1")
                .requires { it.sender.hasPermission("quem.debug") }
                .executes { ctx ->
                    val player = (ctx.source.executor.takeIf { it is Player } ?: return@executes Command.SINGLE_SUCCESS) as Player

                    if (player.party == null) {
                        PartyCreateUI(player)
                    } else {
                        PartyMenuUI(player, player.party!!)
                    }

                    Command.SINGLE_SUCCESS
                })
            .then(Commands.literal("debug2")
                .requires { it.sender.hasPermission("quem.debug") }
                .executes { ctx ->
                    val player = (ctx.source.executor.takeIf { it is Player } ?: return@executes Command.SINGLE_SUCCESS) as Player
                    QuestUI(player)
                    Command.SINGLE_SUCCESS
                })
            .then(Commands.literal("grant")
                .requires { it.sender.hasPermission("quem.grant") }
                .then(Commands.argument("targets", ArgumentTypes.players())
                    .then(Commands.argument("type", QuestTypeArgumentType)
                        .executes(this::grantCommand))))
            .then(Commands.literal("progress")
                .requires { it.sender.hasPermission("quem.progress") }
                .then(Commands.argument("target", ArgumentTypes.player())
                    .then(Commands.argument("requirement", StringArgumentType.word())
                        .then(Commands.argument("formula", FormulaArgumentType)
                            .executes(this::progressCommand)))))
            .then(Commands.literal("reload")
                .requires { it.sender.hasPermission("quem.reload") }
                .executes(this::reloadCommand))
            .then(Commands.literal("revoke")
                .requires { it.sender.hasPermission("quem.revoke") }
                .then(Commands.argument("targets", ArgumentTypes.players())
                    .then(Commands.argument("type", QuestTypeArgumentType)
                        .executes(this::revokeCommand))))
            .then(Commands.literal("stage")
                .requires { it.sender.hasPermission("quem.stage") }
                .then(Commands.literal("mount")
                    .then(Commands.argument("targets", ArgumentTypes.players())
                        .then(Commands.argument("stage", StageArgumentType)
                            .executes(this::stageMountCommand))))
                .then(Commands.literal("unmount")
                    .then(Commands.argument("targets", ArgumentTypes.players())
                        .then(Commands.argument("stage", StageArgumentType)
                            .executes(this::stageUnmountCommand)))))
    }

    private fun grantCommand(ctx: CommandContext<CommandSourceStack>): Int {
        val targets = ctx.getArgument("targets", PlayerSelectorArgumentResolver::class.java).resolve(ctx.source)
        val type = ctx.getArgument("type", QuestType::class.java)

        for (target in targets) {
            val map = target.questTypeMap

            if (map.containsKey(type)) {
                continue
            }

            map[type] = 0
            target.questTypeMap = map
        }

        return Command.SINGLE_SUCCESS
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

    private fun reloadCommand(ctx: CommandContext<CommandSourceStack>): Int {
        QuemLoader.load()
        return Command.SINGLE_SUCCESS
    }

    private fun revokeCommand(ctx: CommandContext<CommandSourceStack>): Int {
        val targets = ctx.getArgument("targets", PlayerSelectorArgumentResolver::class.java).resolve(ctx.source)
        val type = ctx.getArgument("type", QuestType::class.java)

        for (target in targets) {
            target.questTypeMap = target.questTypeMap.also { it.remove(type) }
        }

        return Command.SINGLE_SUCCESS
    }

    private fun stageMountCommand(ctx: CommandContext<CommandSourceStack>): Int {
        val targets = ctx.getArgument("targets", PlayerSelectorArgumentResolver::class.java).resolve(ctx.source)
        val stage = ctx.getArgument("stage", Stage::class.java)

        for (target in targets) {
            target.party?.takeIf { it.hasQuest() }?.let {
                stage.queue.add(it)
            }
        }

        return Command.SINGLE_SUCCESS
    }

    private fun stageUnmountCommand(ctx: CommandContext<CommandSourceStack>): Int {
        val targets = ctx.getArgument("targets", PlayerSelectorArgumentResolver::class.java).resolve(ctx.source)
        val stage = ctx.getArgument("stage", Stage::class.java)

        for (target in targets) {
            target.party?.takeIf { it.hasQuest() }?.let {
                stage.unmount(it)
            }
        }

        return Command.SINGLE_SUCCESS
    }
}