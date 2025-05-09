package net.azisaba.quem.registry

import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed

open class Registry<T: Keyed> {
    private val map = linkedMapOf<Key, T>()

    val entries: Set<T>
        get() = map.values.toSet()

    val size: Int
        get() = map.size

    fun get(key: Key): T? {
        return map[key]
    }

    fun getOrThrow(key: Key): T {
        return get(key) ?: throw NullPointerException()
    }

    fun getOrDefault(key: Key, default: T): T {
        return get(key) ?: default
    }

    fun <E: T> register(entry: E): E {
        map[entry.key()] = entry
        return entry
    }

    fun unregister(key: Key): T {
        val value = get(key) ?: throw IllegalArgumentException("'${key.asString()}' is an invalid key")
        map.remove(key)
        return value
    }
}