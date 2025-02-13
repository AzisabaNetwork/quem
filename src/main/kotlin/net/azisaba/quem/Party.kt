package net.azisaba.quem

import net.azisaba.quem.impl.PartyImpl
import net.azisaba.quem.impl.SoloPartyImpl
import org.bukkit.entity.Player

interface Party: Iterable<Player> {
    companion object {
        const val MAX_SIZE = 9

        private val instances: MutableSet<Party> = mutableSetOf()

        fun create(leader: Player): Party {
            return PartyImpl(leader).also { instances.add(it) }
        }

        fun solo(player: Player): Party {
            return SoloPartyImpl(player).also { instances.add(it) }
        }

        val Player.party: Party?
            get() = instances.firstOrNull { it.isMember(player) }
    }

    var quest: Quest?

    var leader: Player

    val members: Set<Player>

    val size: Int

    fun hasQuest(): Boolean

    fun addMember(member: Player)

    fun removeMember(member: Player)

    fun isMember(player: Player?): Boolean

    fun isNotMember(player: Player?): Boolean {
        return ! isMember(player)
    }

    fun hasPermission(type: QuestType): Boolean

    fun disband() {
        instances.remove(this)
    }
}