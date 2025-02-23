package net.azisaba.quem

import net.azisaba.quem.impl.ScriptImpl

interface Script {
    companion object {
        private val nameRegex = Regex("(${Trigger.entries.joinToString("|") { it.name.lowercase() }})\\+(\\d+)?")

        fun create(name: String, commands: List<String>): Script {
            val matchResult = nameRegex.find(name) ?: return create(Trigger.UNKNOWN.name.lowercase(), commands)

            val trigger = Trigger.valueOf(matchResult.groupValues[1].uppercase())
            val delay = if (matchResult.groups.size == 3) matchResult.groupValues[2].toLong() else 0

            return ScriptImpl(trigger, delay, commands)
        }
    }

    enum class Trigger {
        CANCEL,
        COMPLETE,
        END,
        START,
        UNKNOWN
    }

    val trigger: Trigger

    val delay: Long

    val commands: List<String>

    fun run(quest: Quest)
}