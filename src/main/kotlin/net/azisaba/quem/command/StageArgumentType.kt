package net.azisaba.quem.command

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import net.azisaba.quem.Stage
import net.azisaba.quem.registry.Stages
import net.kyori.adventure.key.Key
import java.util.concurrent.CompletableFuture

object StageArgumentType: CustomArgumentType<Stage, Key> {
    override fun getNativeType(): ArgumentType<Key> {
        return ArgumentTypes.key()
    }

    override fun parse(reader: StringReader): Stage {
        val key = ArgumentTypes.key().parse(reader)
        return Stages.get(key)!!
    }

    override fun <S : Any> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        return CompletableFuture.supplyAsync {
            Stages.entries.map { builder.suggest(it.key.asString()) }
            builder.build()
        }
    }
}