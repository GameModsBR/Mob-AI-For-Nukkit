package br.com.gamemods.mobai.level.spawning

import br.com.gamemods.mobai.entity.EntityCategory
import cn.nukkit.entity.EntityType
import cn.nukkit.level.chunk.IChunk
import cn.nukkit.math.Vector3i
import java.util.*

fun EntityCategory.pickRandomSpawnEntry(chunk: IChunk, chunkIndex: Vector3i, random: Random): SpawnEntry? {
    TODO()
}

fun EntityCategory.containsSpawnEntry(chunk: IChunk, chunkIndex: Vector3i, spawnEntry: SpawnEntry): Boolean {
    TODO()
}

data class SpawnEntry(
    val type: EntityType<*>,
    val minGroupSize: Int,
    val maxGroupSize: Int
) {
    init {
        require(minGroupSize >= 0) { "Negative min group size. $minGroupSize" }
        require(maxGroupSize >= minGroupSize) { "Max group size must be higher then min. Min:$minGroupSize, Max:$maxGroupSize" }
    }
}
