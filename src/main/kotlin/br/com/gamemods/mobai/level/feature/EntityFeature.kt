package br.com.gamemods.mobai.level.feature

import br.com.gamemods.mobai.entity.EntityCategory
import br.com.gamemods.mobai.level.spawning.SpawnEntry
import cn.nukkit.level.generator.feature.Feature

interface EntityFeature: Feature {
    val monsterSpawns: List<SpawnEntry>
    val creatureSpawns: List<SpawnEntry>

    fun getMonstersOrCreatures(category: EntityCategory): List<SpawnEntry>? {
        return when (category) {
            EntityCategory.MONSTER -> monsterSpawns
            EntityCategory.CREATURE -> creatureSpawns
            else -> null
        }
    }

    fun getEntitySpawnList(category: EntityCategory): List<SpawnEntry> {
        return getMonstersOrCreatures(category) ?: emptyList()
    }
}
