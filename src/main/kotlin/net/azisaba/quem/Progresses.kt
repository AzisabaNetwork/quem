package net.azisaba.quem

class Progresses internal constructor(private val quest: Quest): Iterable<MutableMap. MutableEntry<QuestRequirement, Int>> {
    private val map = quest.type.requirements.associateWith { 0 }.toMutableMap()

    operator fun get(key: String): Int {
        val requirement = map.keys.firstOrNull { it.key == key } ?: throw IllegalArgumentException("No requirement '$key' found for '${quest.type.key.asString()}'")
        return get(requirement)
    }

    operator fun get(requirement: QuestRequirement): Int {
        return map[requirement] ?: throw IllegalArgumentException("${requirement.key} is a requirement that is not defined in ${quest.type.key.asString()}")
    }

    operator fun set(requirement: QuestRequirement, progress: Int) {
        require(map.containsKey(requirement)) { "${requirement.key} is a requirement that is not defined in ${quest.type.key.asString()}" }
        map[requirement] = progress

        if (map.all { it.key.amount <= it.value }) {
            quest.end(Quest.EndReason.COMPLETE)
        }
    }

    override fun iterator(): Iterator<MutableMap.MutableEntry<QuestRequirement, Int>> {
        return map.iterator()
    }
}