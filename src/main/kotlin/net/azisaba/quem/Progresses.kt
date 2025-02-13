package net.azisaba.quem

class Progresses internal constructor(private val quest: Quest): Iterable<MutableMap. MutableEntry<QuestRequirement, Int>> {
    private val map = quest.type.requirements.associateWith { 0 }.toMutableMap()

    operator fun get(requirement: QuestRequirement): Int {
        return map[requirement] ?: throw IllegalArgumentException("${requirement.key} is a requirement that is not defined in ${quest.type.key}.")
    }

    operator fun set(requirement: QuestRequirement, progress: Int) {
        require(map.containsKey(requirement)) { "${requirement.key} is a requirement that is not defined in ${quest.type.key}" }
        map[requirement] = progress

        if (map.all { it.key.amount <= it.value }) {
            quest.onEnd(Quest.EndReason.COMPLETE)
        }
    }

    override fun iterator(): Iterator<MutableMap.MutableEntry<QuestRequirement, Int>> {
        return map.iterator()
    }
}