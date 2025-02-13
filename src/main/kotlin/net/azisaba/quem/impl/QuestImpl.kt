package net.azisaba.quem.impl

import net.azisaba.quem.*
import net.azisaba.quem.gui.QuestPanelUI
import net.azisaba.quem.util.hasPermission
import net.azisaba.quem.util.questTypeMap
import org.bukkit.Location
import org.bukkit.entity.Player

class QuestImpl(override val type: QuestType, override val party: Party) : Quest {
    override val players: Set<Player>
        get() = _players

    private val originalLocations: Map<Player, Location> = party.members.associateWith { it.location }

    override val progresses = Progresses(this)

    override val panel: QuestPanelUI

    private val _players = party.members.toMutableSet()

    init {
        if (type.maxPlayers != null && type.maxPlayers < party.size) {
            throw IllegalArgumentException("The maximum number of players specified by ${type.key.asString()} has been exceeded: ${party.size}")
        }

        if (party.any { ! it.hasPermission(type) }) {
            throw IllegalArgumentException("The party includes a player who does not have the permission to play")
        }

        panel = QuestPanelUI(this)
        party.quest = this

        party.members.forEach {
            panel.addAudience(it)
            it.teleport(type.location)
        }
    }

    override fun removePlayer(player: Player) {
        if (isNotPlayer(player)) {
            throw IllegalArgumentException("${player.name} is not a player of the quest")
        }

        _players.remove(player)
        panel.removeAudience(player)
        val lobbyConfig = Quem.pluginConfig.lobby
        player.teleport(if (lobbyConfig != null) net.azisaba.quem.data.Location(lobbyConfig) else originalLocations[player]!!)
    }

    override fun isPlayer(player: Player?): Boolean {
        return players.contains(player)
    }

    override fun end(reason: Quest.EndReason) {
        super.end(reason)

        party.quest = null
        panel.kill()

        for (member in party) {
            member.questTypeMap = member.questTypeMap.also {
                it[type] = (it[type] ?: 0) + 1
            }
        }
    }
}