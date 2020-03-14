package br.com.gamemods.mobai.level.spawning

import br.com.gamemods.mobai.entity.EntityCategory
import br.com.gamemods.mobai.level.getBiome
import cn.nukkit.level.chunk.IChunk
import cn.nukkit.math.Vector3i

interface EntityGenerator {
    fun getEntitySpawnList(category: EntityCategory, chunk: IChunk, chunkIndex: Vector3i): List<SpawnEntry> {
        return chunk.getBiome(chunkIndex).getEntitySpawnList(category)
    }
}
