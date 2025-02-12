package net.azisaba.quem.command

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import net.azisaba.quem.QuestType
import net.azisaba.quem.registry.QuestTypes
import net.kyori.adventure.key.Key
import java.util.concurrent.CompletableFuture

object QuestTypeArgumentType: CustomArgumentType<QuestType, Key> {
    override fun getNativeType(): ArgumentType<Key> {
        return ArgumentTypes.key()
    }

    override fun parse(reader: StringReader): QuestType {
        val key = ArgumentTypes.key().parse(reader)
        return QuestTypes.get(key)!!
    }

    override fun <S : Any> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        return CompletableFuture.supplyAsync {
            QuestTypes.entries.map { builder.suggest(it.key.asString()) }
            builder.build()
        }
    }
}