package net.azisaba.quem

import net.azisaba.quem.registry.Keyed
import net.azisaba.quem.extension.toTextComponent
import net.kyori.adventure.key.Key
import org.bukkit.Location
import org.bukkit.entity.Player

class Stage(override val key: Key, private val data: net.azisaba.quem.data.Stage): StageLike, Keyed {
    val title = data.title.toTextComponent()

    val location: Location
        get() = net.azisaba.quem.data.Location(data.location)

    val unmountLocation: Location?
        get() = run {
            if (data.unmountLocation != null) {
                net.azisaba.quem.data.Location(data.unmountLocation)
            } else {
                null
            }
        }

    val maxParties = data.maxParties

    val queue = Queue(this)

    val size: Int
        get() = parties.size

    override val stage: Stage
        get() = this

    private val parties = mutableSetOf<Party>()
    private val originalLocations = mutableMapOf<Player, Location>()

    private fun mount(party: Party) {
        if (isMounted(party)) {
            throw IllegalArgumentException("The party is already mounted.")
        }

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
        if (! isMounted(party)) {
            throw IllegalArgumentException("The party is not mounted.")
        }

        party.stage = null

        party.forEach {
            it.teleport(if (unmountLocation == null) originalLocations[it]!! else unmountLocation!!)
            originalLocations.remove(it)
        }

        parties.remove(party)
        queue.first?.let { mount(it) }
    }

    fun isMounted(party: Party): Boolean {
        return parties.contains(party)
    }

    class Queue(override val stage: Stage): StageLike {
        val first: Party?
            get() = set.firstOrNull()

        val size: Int
            get() = set.size

        private val set = linkedSetOf<Party>()

        fun add(party: Party): Boolean {
            if (stage.parties.size < stage.maxParties) {
                stage.mount(party)
                return true
            }

            set.add(party)
            party.stage = this
            return false
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