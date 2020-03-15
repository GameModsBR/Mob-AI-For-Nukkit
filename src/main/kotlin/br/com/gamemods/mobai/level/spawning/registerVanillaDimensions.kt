package br.com.gamemods.mobai.level.spawning

import br.com.gamemods.mobai.MobAIPlugin
import br.com.gamemods.mobai.entity.EntityCategory
import br.com.gamemods.mobai.level.feature.FeatureRegistry
import br.com.gamemods.mobai.level.feature.VanillaFeatureTypes.NETHER_BRIDGE
import br.com.gamemods.mobai.level.feature.VanillaFeatureTypes.OCEAN_MONUMENT
import br.com.gamemods.mobai.level.feature.VanillaFeatureTypes.PILLAGER_OUTPOST
import br.com.gamemods.mobai.level.feature.VanillaFeatureTypes.SWAMP_HUT
import br.com.gamemods.mobai.level.getBlockId
import cn.nukkit.block.BlockIds.NETHER_BRICK
import cn.nukkit.level.Level
import cn.nukkit.level.chunk.IChunk
import cn.nukkit.math.Vector3i

internal fun MobAIPlugin.registerVanillaDimensions() {
    logger.debug("Registering dimensions")
    Level.DIMENSION_OVERWORLD.defaultEntityGenerator = OverworldEntityGenerator()
    Level.DIMENSION_NETHER.defaultEntityGenerator = NetherEntityGenerator()
}

open class OverworldEntityGenerator: EntityGenerator {
    override fun getEntitySpawnList(
        category: EntityCategory,
        chunk: IChunk,
        chunkIndex: Vector3i,
        levelPos: Vector3i
    ): List<SpawnEntry> {
        val level = chunk.level
        var insideHut = false
        FeatureRegistry[SWAMP_HUT]
            .takeIf { insideHut = it.isInsideHut(level, levelPos); insideHut }
            ?.getMonstersOrCreatures(category)
            ?.let { return it }

        if (!insideHut && category == EntityCategory.MONSTER) {
            FeatureRegistry[PILLAGER_OUTPOST]
                .takeIf { it.isInsideStructure(level, levelPos, true) }
                ?.let { return it.monsterSpawns }

            FeatureRegistry[OCEAN_MONUMENT]
                .takeIf { it.isInsideStructure(level, levelPos, true) }
                ?.let { return it.monsterSpawns }
        }

        return super.getEntitySpawnList(category, chunk, chunkIndex, levelPos)
    }
}

open class NetherEntityGenerator: EntityGenerator {
    override fun getEntitySpawnList(
        category: EntityCategory,
        chunk: IChunk,
        chunkIndex: Vector3i,
        levelPos: Vector3i
    ): List<SpawnEntry> {
        if (category == EntityCategory.MONSTER) {
            val level = chunk.level
            val fortress = FeatureRegistry[NETHER_BRIDGE]
            if (fortress.isInsideStructure(level, levelPos)) {
                return fortress.monsterSpawns
            }
            if (chunk.getBlockId(chunkIndex.down()) == NETHER_BRICK
                && fortress.isInsideStructure(level, levelPos, true)) {
                return fortress.monsterSpawns
            }
        }
        return super.getEntitySpawnList(category, chunk, chunkIndex, levelPos)
    }
}
