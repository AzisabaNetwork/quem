package net.azisaba.quem

import net.azisaba.quem.registry.Keyed
import net.azisaba.quem.util.toTextComponent
import net.kyori.adventure.key.Key
import org.bukkit.Location
import org.bukkit.entity.Player

class Stage(override val key: Key, private val data: net.azisaba.quem.data.Stage): StageLike, Keyed {
    val title = data.title.toTextComponent()

    val location: Location
        get() = net.azisaba.quem.data.Location(data.location)

    val maxParties = data.maxParties

    val queue = Queue(this)

    val size: Int
        get() = parties.size

    override val stage: Stage
        get() = this

    private val parties = mutableSetOf<Party>()
    private val originalLocations = mutableMapOf<Player, Location>()

    private fun mount(party: Party) {
        if (maxParties <= size) {
            throw UnsupportedOperationException()
        }

        parties.add(party)
        party.stage = this

        party.forEach {
            originalLocations[it] = it.location
            it.teleport(location)
        }
    }

    fun unmount(party: Party) {
        party.stage = null

        party.forEach {
            it.teleport(originalLocations[it]!!)
            originalLocations.remove(it)
        }

        parties.remove(party)
        queue.first?.let { mount(it) }
    }

    class Queue(override val stage: Stage): StageLike {
        val first: Party?
            get() = set.firstOrNull()

        val size: Int
            get() = set.size

        private val set = linkedSetOf<Party>()

        fun add(party: Party) {
            if (stage.parties.size < stage.maxParties) {
                stage.mount(party)
                return
            }

            set.add(party)
            party.stage = this
        }

        fun remove(party: Party) {
            party.stage = null
            set.remove(party)
        }

        fun indexOf(party: Party): Int {
            return set.indexOf(party)
        }
    }
}

interface StageLike {
    val stage: Stage
}