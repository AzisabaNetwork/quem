package net.azisaba.quem

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import net.azisaba.quem.listener.PlayerListener
import net.azisaba.quem.registry.QuestTypes
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class Quem : JavaPlugin() {
    companion object {
        const val PLUGIN_ID = "quem"

        lateinit var plugin: Quem

        val pluginLogger: ComponentLogger
            get() = plugin.componentLogger

        val pluginDirectory: File
            get() = plugin.dataFolder
    }

    override fun onEnable() {
        plugin = this

        server.pluginManager.registerEvents(PlayerListener, this)

        if (! pluginDirectory.exists() && ! pluginDirectory.mkdir()) {
            pluginLogger.error(Component.text("Failed to create plugin directory.").color(NamedTextColor.RED))
        }

        pluginDirectory.walk().forEach { file ->
            if (! file.isFile || (file.extension != "yml" && file.extension != "yaml")) {
                return@forEach
            }

            val data: net.azisaba.quem.data.QuestType = Yaml.default.decodeFromStream(file.inputStream())

            QuestTypes.register(QuestType(
                key = Key.key(PLUGIN_ID, file.nameWithoutExtension),
                data = data
            ))
        }
    }
}