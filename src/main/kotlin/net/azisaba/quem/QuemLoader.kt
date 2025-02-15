package net.azisaba.quem

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import net.azisaba.quem.registry.QuestCategories
import net.azisaba.quem.registry.QuestTypes
import net.azisaba.quem.registry.Stages
import net.kyori.adventure.key.Key
import java.io.File

object QuemLoader {
    private val namespaceDirectoryRegex = Regex("^@([a-zA-Z0-9_-]+)$")
    private val keyRegex = Regex("^[a-zA-Z0-9_-]+$")

    private val namespaces: Map<String, File>
        get() = Quem.pluginDirectory
            .listFiles()
            ?.filter { it.isDirectory && it.name.matches(namespaceDirectoryRegex) }
            ?.associate { it.name.substring(1) to it }
            ?: emptyMap()

    fun load() {
        Quest.all.forEach { it.end(Quest.EndReason.RELOAD) }

        QuestCategories.entries
            .filter { it !is BuildInQuestCategory }
            .forEach { QuestCategories.unregister(it.key) }

        QuestTypes.entries.forEach { QuestTypes.unregister(it.key) }

        Stages.entries.forEach { Stages.unregister(it.key) }

        for ((namespace, directory) in namespaces) {
            File(directory, "categories").takeIf { it.exists() }?.let {
                loadQuestCategories(namespace, it)
            }

            File(directory, "types").takeIf { it.exists() }?.let {
                loadQuestTypes(namespace, it)
            }

            File(directory, "stages").takeIf { it.exists() }?.let {
                loadStages(namespace, it)
            }
        }
    }

    private fun loadQuestCategories(namespace: String, directory: File) {
        directory.walk()
            .filter { it.isFile && it.nameWithoutExtension.matches(keyRegex) && it.extension == "yml" }
            .forEach { file ->
                val data = Yaml.default.decodeFromStream<net.azisaba.quem.data.QuestCategory>(file.inputStream())

                QuestCategories.register(QuestCategory(
                    key = Key.key(namespace, file.nameWithoutExtension),
                    data = data
                ))
            }
    }

    private fun loadQuestTypes(namespace: String, directory: File) {
        directory.walk()
            .filter { it.isFile && it.nameWithoutExtension.matches(keyRegex) && it.extension == "yml" }
            .forEach { file ->
                val data = Yaml.default.decodeFromStream<net.azisaba.quem.data.QuestType>(file.inputStream())

                QuestTypes.register(QuestType(
                    key = Key.key(namespace, file.nameWithoutExtension),
                    data = data
                ))
            }
    }

    private fun loadStages(namespace: String, directory: File) {
        directory.walk()
            .filter { it.isFile && it.nameWithoutExtension.matches(keyRegex) && it.extension == "yml" }
            .forEach { file ->
                val data = Yaml.default.decodeFromStream<net.azisaba.quem.data.Stage>(file.inputStream())

                Stages.register(Stage(
                    key = Key.key(namespace, file.nameWithoutExtension),
                    data = data
                ))
            }
    }
}