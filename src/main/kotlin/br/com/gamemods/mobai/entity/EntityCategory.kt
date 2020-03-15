package br.com.gamemods.mobai.entity

import br.com.gamemods.mobai.level.Dimension
import br.com.gamemods.mobai.level.dimensionType
import br.com.gamemods.mobai.level.spawning.LevelSettings
import cn.nukkit.level.Level

enum class EntityCategory(val spawnCap: Int, val isPeaceful: Boolean, val isAnimal: Boolean) {
    MONSTER(70, false, true),
    CREATURE(10, true, true),
    AMBIENT(15, true, false),
    WATER_CREATURE(15, true, false),
    MISC(15, true, false),
    UNKNOWN(70, true, false)
    ;

    inline val isMiscOrUnknown get() = this === MISC || this === UNKNOWN

    private inline val LevelSettings.customCap: Int? get() {
        return customCategoryCaps[this@EntityCategory]
    }

    fun getEffectiveCap(dim: Dimension) : Int {
        return LevelSettings.defaultByDimension[dim]?.customCap ?: LevelSettings.fallback.customCap ?: spawnCap
    }

    fun getEffectiveCap(level: Level): Int {
        return LevelSettings[level]?.customCap ?: getEffectiveCap(level.dimensionType)
    }
}
