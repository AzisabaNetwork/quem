package net.azisaba.quem.data

import kotlinx.serialization.Serializable
import net.azisaba.quem.util.toKey
import org.bukkit.Bukkit

@Serializable
data class Location(
    val world: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float = 0.0f,
    val pitch: Float = 0.0f
)

fun Location(location: Location): org.bukkit.Location {
    return org.bukkit.Location(Bukkit.getWorld(location.world.toKey()), location.x, location.y, location.z, location.yaw, location.pitch)
}