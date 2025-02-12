package net.azisaba.quem.impl

import com.tksimeji.visualkit.Visualkit
import net.azisaba.quem.Party
import net.azisaba.quem.Quest
import net.azisaba.quem.gui.PartyUI
import org.bukkit.entity.Player

open class PartyImpl(entrepreneur: Player): Party {
    override var quest: Quest? = null

    final override var leader: Player = entrepreneur
        set(value) {
            if (isNotMember(value)) {
                throw IllegalArgumentException()
            }

            field = value
            updateGui()
        }

    override val members: Set<Player>
        get() = _members.toSet()

    override val size: Int
        get() = members.size

    private val _members: MutableSet<Player> = mutableSetOf(leader)

    override fun hasQuest(): Boolean {
        return quest != null
    }

    override fun addMember(member: Player) {
        if (hasQuest()) {
            throw UnsupportedOperationException("Members cannot be added to a party during a quest.")
        }

        if (Party.MAX_SIZE <= size) {
            throw UnsupportedOperationException("The maximum party size is ${Party.MAX_SIZE}. No more members can be added.")
        }

        _members.add(member)
        updateGui()
    }

    override fun removeMember(member: Player) {
        _members.remove(member)

        if (member == leader || size <= 0) {
            disband()
            return
        }

        updateGui()
    }

    override fun isMember(player: Player?): Boolean {
        return _members.contains(player)
    }

    override fun isNotMember(player: Player?): Boolean {
        return ! isMember(player)
    }

    override fun disband() {
        Visualkit.getSessions(PartyUI::class.java)
            .filter { it.party == this }
            .forEach(PartyUI::close)
    }

    override fun iterator(): Iterator<Player> {
        return members.iterator()
    }

    private fun updateGui() {
        Visualkit.getSessions(PartyUI::class.java)
            .filter { it.party == this }
            .forEach(PartyUI::update)
    }
}