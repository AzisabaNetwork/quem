package net.azisaba.quem.impl

import net.azisaba.quem.Quem
import net.azisaba.quem.Quest
import net.azisaba.quem.Script
import org.bukkit.Bukkit

class ScriptImpl(
    override val trigger: Script.Trigger,
    override val delay: Long,
    override val commands: List<String>
): Script {
    override fun run(quest: Quest) {
        Bukkit.getScheduler().runTaskLater(Quem.plugin, { ->
            for (command in commands) {
                if (command.startsWith(':')) {
                    for (player in quest.players) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.substring(1).replace("%", player.name))
                    }
                } else {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)
                }
            }
        }, delay)
    }
}