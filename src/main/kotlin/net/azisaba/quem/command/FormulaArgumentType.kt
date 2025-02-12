package net.azisaba.quem.command

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import net.azisaba.quem.util.CommandSyntaxException
import java.util.concurrent.CompletableFuture

object FormulaArgumentType: CustomArgumentType<FormulaArgumentType.Formula, String> {
    private val regex = Regex("([+\\-*/%=]?)(\\d+)")

    override fun getNativeType(): ArgumentType<String> {
        return StringArgumentType.string()
    }

    override fun parse(reader: StringReader): Formula {
        val input = reader.readString()
        val result = regex.find(input)

        return if (result != null) {
            val operator = Operator.operator(result.groupValues[1].ifEmpty { "=" })!!
            val number = result.groupValues[2]
            Formula(operator = operator, number = number.toInt())
        } else {
            throw CommandSyntaxException()
        }
    }

    override fun <S : Any> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        return CompletableFuture.supplyAsync {
            Operator.entries.forEach { builder.suggest(it.literal) }
            builder.build()
        }
    }

    enum class Operator(val literal: String, val calculator: (Int, Int) -> Int) {
        ADD("+", { base, number -> base + number }),
        SUBTRACT("-", { base, number -> base - number }),
        MULTIPLY("*", { base, number -> base * number }),
        DIVIDE("/", { base, number -> base / number }),
        EQUAL("=", { _, number -> number });

        companion object {
            fun operator(string: String): Operator? {
                return entries.firstOrNull { it.literal == string }
            }
        }
    }

    data class Formula(val operator: Operator, val number: Int) {
        fun calculate(base: Int): Int {
            return operator.calculator(base, number)
        }
    }
}