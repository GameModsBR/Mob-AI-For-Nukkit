package br.com.gamemods.mobai.level.spawning

import br.com.gamemods.mobai.entity.EntityCategory
import cn.nukkit.level.biome.Biome
import java.util.*

private val biomeEntries = mutableMapOf<Biome, EnumMap<EntityCategory, MutableList<SpawnEntry>>>()

fun Biome.getEntitySpawnList(category: EntityCategory): MutableList<SpawnEntry> {
    return biomeEntries
        .getOrPut(this) { EnumMap(EntityCategory::class.java) }
        .getOrPut(category, ::mutableListOf)
}

fun Biome.addSpawn(category: EntityCategory, vararg entry: SpawnEntry) {
    getEntitySpawnList(category) += entry
}
