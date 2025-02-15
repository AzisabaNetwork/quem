package net.azisaba.quem.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import net.azisaba.quem.*
import net.azisaba.quem.Party.Companion.party
import net.azisaba.quem.gui.PartyCreateUI
import net.azisaba.quem.gui.PartyMenuUI
import net.azisaba.quem.gui.QuestUI
import net.azisaba.quem.extension.CommandSyntaxException
import net.kyori.adventure.text.Component
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

        val changes = mutableListOf<Player>()

        for (target in targets) {
            val map = target.questTypeMap

            if (map.containsKey(type)) {
                continue
            }

            map[type] = 0
            target.questTypeMap = map
            changes.add(target)
        }

        if (changes.isEmpty()) {
            ctx.source.sender.sendMessage(Component.text("No changed were made."))
            return Command.SINGLE_SUCCESS
        }

        ctx.source.sender.sendMessage(Component.text("Granted '${type.key.asString()}' for ${changes.joinToString(", ") { it.name }}."))
        return Command.SINGLE_SUCCESS
    }

    private fun progressCommand(ctx: CommandContext<CommandSourceStack>): Int {
        val target = ctx.getArgument("target", PlayerSelectorArgumentResolver::class.java).resolve(ctx.source).first()
        val quest = target.party?.quest ?: throw CommandSyntaxException("No quests related to ${target.name} were found")
        val requirement = quest.type.requirements.firstOrNull { it.key == ctx.getArgument("requirement", String::class.java) } ?: throw CommandSyntaxException("Unknown requirement")

        val base = quest.progresses[requirement]
        val formula = ctx.getArgument("formula", FormulaArgumentType.Formula::class.java)

        quest.progresses[requirement] = formula.calculate(base)

        ctx.source.sender.sendMessage(Component.text("Changed quest requirement '${requirement.key}' progress to ${quest.progresses[requirement]}."))
        return Command.SINGLE_SUCCESS
    }

    private fun reloadCommand(ctx: CommandContext<CommandSourceStack>): Int {
        QuemLoader.load()
        Quem.plugin.reloadPluginConfig()
        ctx.source.sender.sendMessage(Component.text("Reload completed."))
        return Command.SINGLE_SUCCESS
    }

    private fun revokeCommand(ctx: CommandContext<CommandSourceStack>): Int {
        val targets = ctx.getArgument("targets", PlayerSelectorArgumentResolver::class.java).resolve(ctx.source)
        val type = ctx.getArgument("type", QuestType::class.java)

        val changes = mutableListOf<Player>()

        for (target in targets) {
            if (! target.questTypeMap.containsKey(type)) {
                continue
            }

            target.questTypeMap = target.questTypeMap.also { it.remove(type) }
            changes.add(target)
        }

        if (changes.isEmpty()) {
            ctx.source.sender.sendMessage(Component.text("No changed were made."))
            return Command.SINGLE_SUCCESS
        }

        ctx.source.sender.sendMessage(Component.text("Revoked '${type.key.asString()}' for ${changes.joinToString(", ") { it.name }}."))
        return Command.SINGLE_SUCCESS
    }

    private fun stageMountCommand(ctx: CommandContext<CommandSourceStack>): Int {
        val targets = ctx.getArgument("targets", PlayerSelectorArgumentResolver::class.java).resolve(ctx.source)
        val stage = ctx.getArgument("stage", Stage::class.java)

        var mounts = 0
        var queues = 0

        for (target in targets) {
            target.party?.takeIf { it.hasQuest() }?.let {
                if (stage.queue.add(it)) {
                    mounts += 1
                } else {
                    queues += 1
                }
            }
        }

        ctx.source.sender.sendMessage(Component.text("$mounts party(s) mounted, $queues party(s) added to queue."))
        return Command.SINGLE_SUCCESS
    }

    private fun stageUnmountCommand(ctx: CommandContext<CommandSourceStack>): Int {
        val targets = ctx.getArgument("targets", PlayerSelectorArgumentResolver::class.java).resolve(ctx.source)
        val stage = ctx.getArgument("stage", Stage::class.java)

        var unmounts = 0

        for (target in targets) {
            target.party?.takeIf { it.hasQuest() }?.let {
                if (stage.isMounted(it)) {
                    return@let
                }

                stage.unmount(it)
                unmounts -= 1
            }
        }

        ctx.source.sender.sendMessage(Component.text("$unmounts party(s) unmounted."))
        return Command.SINGLE_SUCCESS
    }
}