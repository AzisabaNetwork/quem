package net.azisaba.quem

import net.azisaba.quem.data.Guide
import net.azisaba.quem.data.Location
import net.azisaba.quem.util.toTextComponent
import net.kyori.adventure.text.Component

class Guide(private val data: Guide) {
    val title: Component?
        get() = data.title?.toTextComponent()

    val location
        get() = Location(data.location)

    fun isFilled(quest: Quest): Boolean {
        return data.requirements.toMap().all { quest.progresses[it.key] == it.value }
    }
}