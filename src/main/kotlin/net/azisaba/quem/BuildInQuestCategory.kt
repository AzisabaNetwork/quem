package net.azisaba.quem

import net.azisaba.quem.data.Icon
import net.azisaba.quem.util.toComponentString
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.Material

class BuildInQuestCategory(
    key: Key,
    name: Component,
    iconType: Material,
    iconModel: Key? = null,
    iconAura: Boolean = false
) : QuestCategory(key, net.azisaba.quem.data.QuestCategory(
    name = name.toComponentString(),
    icon = Icon(
        type = iconType.key().asString(),
        model = iconModel?.asString(),
        aura = iconAura
    )
))