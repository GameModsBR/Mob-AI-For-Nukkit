package br.com.gamemods.mobai.level.spawning

import br.com.gamemods.mobai.entity.EntityCategory
import br.com.gamemods.mobai.level.Dimension
import br.com.gamemods.mobai.level.dimensionType
import cn.nukkit.level.Level
import java.util.*

data class LevelSettings(
    var difficulty: Int = -1,
    var spawnAnimals: Boolean? = null,
    var spawnMonsters: Boolean? = null,
    var customCategoryCaps: EnumMap<EntityCategory, Int> = EnumMap(EntityCategory::class.java)
) {
    companion object {
        private val loaded = WeakHashMap<Level, LevelSettings>()
        internal val defaultByDimension = mutableMapOf<Dimension, LevelSettings>()
        internal var fallback = LevelSettings(spawnAnimals = true, spawnMonsters = true)

        fun getEffective(level: Level) = (get(level) ?: defaultByDimension[level.dimensionType] ?: fallback).copy()
        operator fun get(level: Level) = loaded[level]

        internal operator fun set(level: Level, settings: LevelSettings) = loaded.put(level, settings)
        internal operator fun minusAssign(level: Level) = loaded.minusAssign(level)
    }
}
