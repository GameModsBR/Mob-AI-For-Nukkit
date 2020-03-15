package br.com.gamemods.mobai.level.spawning

import br.com.gamemods.mobai.entity.EntityCategory
import cn.nukkit.level.chunk.IChunk
import cn.nukkit.math.Vector3i
import java.util.*

fun EntityCategory.pickRandomSpawnEntry(chunk: IChunk, chunkIndex: Vector3i, levelPos: Vector3i, random: Random): SpawnEntry? {
    val list = chunk.level.entityGenerator.getEntitySpawnList(this, chunk, chunkIndex, levelPos)
    if (list.isEmpty()) {
        return null
    }
    return list.weightedRandom(random)
}

fun EntityCategory.containsSpawnEntry(chunk: IChunk, chunkIndex: Vector3i, levelPos: Vector3i, spawnEntry: SpawnEntry): Boolean {
    val list = chunk.level.entityGenerator.getEntitySpawnList(this, chunk, chunkIndex, levelPos)
    if (list.isEmpty()) {
        return false
    }
    return spawnEntry in list
}
