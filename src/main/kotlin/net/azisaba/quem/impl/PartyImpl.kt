package net.azisaba.quem.impl

import com.tksimeji.visualkit.Visualkit
import net.azisaba.quem.Party
import net.azisaba.quem.Party.Companion.hasParty
import net.azisaba.quem.Quest
import net.azisaba.quem.QuestType
import net.azisaba.quem.StageLike
import net.azisaba.quem.gui.PartyUI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player

open class PartyImpl(entrepreneur: Player): Party {
    override var quest: Quest? = null

    override var stage: StageLike? = null

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

    override fun hasStage(): Boolean {
        return stage != null
    }

    override fun addMember(member: Player) {
        if (hasQuest()) {
            throw UnsupportedOperationException("Members cannot be added to a party during a quest")
        }

        if (Party.MAX_SIZE <= size) {
            throw UnsupportedOperationException("The maximum party size is ${Party.MAX_SIZE}. No more members can be added.")
        }

        if (member.hasParty()) {
            throw IllegalArgumentException("'${member.name}' is already in a party")
        }

        _members.add(member)
        broadcast(Component.translatable("party.joined", Component.text(member.name)))
        updateGui()
    }

    override fun removeMember(member: Player) {
        if (isNotMember(member)) {
            throw IllegalArgumentException("${member.name} is not a member of the party")
        }

        _members.remove(member)
        quest?.removePlayer(member)

        if (member == leader || size <= 0) {
            disband()
            return
        }

        broadcast(Component.translatable("party.quited", Component.text(member.name)))
        updateGui()
    }

    override fun isMember(player: Player?): Boolean {
        return _members.contains(player)
    }

    override fun hasPermission(type: QuestType): Boolean {
        val maxPlayers = type.maxPlayers
        val minPlayers = type.minPlayers
        return (maxPlayers == null || size <= maxPlayers) && (minPlayers == null || minPlayers <= size)
    }

    override fun disband() {
        members.forEach(this::removeMember)

        Visualkit.getSessions(PartyUI::class.java)
            .filter { it.party == this }
            .forEach(PartyUI::close)
    }

    override fun iterator(): Iterator<Player> {
        return members.iterator()
    }

    override fun broadcast(message: Component) {
        forEach { it.sendMessage(Component.translatable("party.prefix").color(NamedTextColor.BLUE)
            .appendSpace()
            .append(message.colorIfAbsent(NamedTextColor.WHITE))) }
    }

    private fun updateGui() {
        Visualkit.getSessions(PartyUI::class.java)
            .filter { it.party == this }
            .forEach(PartyUI::update)
    }
}