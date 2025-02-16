package net.azisaba.quem.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import net.azisaba.quem.Party
import net.azisaba.quem.Party.Companion.party
import net.azisaba.quem.Party.Invite.Companion.sendPartyInvite
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import java.util.UUID

object PartyCommand {
    fun create(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("party")
            .then(Commands.literal("accept")
                .requires { it.sender is Player }
                .then(Commands.argument("uuid", ArgumentTypes.uuid())
                    .executes(this::acceptCommand)))
            .then(Commands.literal("invite")
                .requires { it.sender is Player }
                .then(Commands.argument("target", ArgumentTypes.player())
                    .executes(this::inviteCommand)))
            .then(Commands.literal("kick")
                .requires { it.sender is Player }
                .then(Commands.argument("target", ArgumentTypes.player())
                    .executes(this::kickCommand)))
    }

    private fun acceptCommand(ctx: CommandContext<CommandSourceStack>): Int {
        val sender = ctx.source.sender as Player
        val invite = Party.Invite.get(ctx.getArgument("uuid", UUID::class.java))

        if (invite == null || invite.player != sender) {
            sender.sendMessage(Component.translatable("command.party.accept.invite_not_found").color(NamedTextColor.RED))
            return Command.SINGLE_SUCCESS
        }

        invite.accept()
        return Command.SINGLE_SUCCESS
    }

    private fun inviteCommand(ctx: CommandContext<CommandSourceStack>): Int {
        val sender = ctx.source.sender as Player
        val party = sender.party
        val target = ctx.getArgument("target", PlayerSelectorArgumentResolver::class.java).resolve(ctx.source).first()

        if (party == null) {
            sender.sendMessage(Component.translatable("command.party.not_found").color(NamedTextColor.RED))
            return Command.SINGLE_SUCCESS
        }

        if (party.isMember(target) || Party.Invite.get(party, target) != null) {
            sender.sendMessage(Component.translatable("command.party.invite.already").color(NamedTextColor.RED))
            return Command.SINGLE_SUCCESS
        }

        if (! party.hasInvitationPermission(sender)) {
            sender.sendMessage(Component.translatable("command.party.permission_error").color(NamedTextColor.RED))
            return Command.SINGLE_SUCCESS
        }

        target.sendPartyInvite(party, sender, target)
        return Command.SINGLE_SUCCESS
    }

    private fun kickCommand(ctx: CommandContext<CommandSourceStack>): Int {
        val sender = ctx.source.sender as Player
        val party = sender.party
        val target = ctx.getArgument("target", PlayerSelectorArgumentResolver::class.java).resolve(ctx.source).first()

        if (party == null) {
            sender.sendMessage(Component.translatable("command.party.not_found").color(NamedTextColor.RED))
            return Command.SINGLE_SUCCESS
        }

        if (party.leader != sender) {
            sender.sendMessage(Component.translatable("command.party.permission_error").color(NamedTextColor.RED))
            return Command.SINGLE_SUCCESS
        }

        if (target == party.leader || party.isNotMember(target)) {
            sender.sendMessage(Component.translatable("command.party.kick.can_not_kick").color(NamedTextColor.RED))
            return Command.SINGLE_SUCCESS
        }

        party.removeMember(target)
        sender.sendMessage(Component.translatable("command.party.kick.kicked"))
        return Command.SINGLE_SUCCESS
    }
}