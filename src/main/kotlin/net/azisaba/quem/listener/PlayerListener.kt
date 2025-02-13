package net.azisaba.quem.listener

import net.azisaba.quem.Party.Companion.party
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerQuitEvent

object PlayerListener: Listener {
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        val party = player.party ?: return
        party.removeMember(player)
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.player
        val party = player.party ?: return
        party.quest?.removePlayer(player)
    }
}