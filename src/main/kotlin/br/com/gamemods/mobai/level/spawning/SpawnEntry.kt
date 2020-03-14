package br.com.gamemods.mobai.level.spawning

import cn.nukkit.entity.EntityType

data class SpawnEntry(
    val type: EntityType<*>,
    override val weight: Int,
    val minGroupSize: Int,
    val maxGroupSize: Int
): Weighted {
    init {
        require(weight > 0) { "Weight must be >= 0. Received:$weight" }
        require(minGroupSize >= 0) { "Negative min group size. $minGroupSize" }
        require(maxGroupSize >= minGroupSize) { "Max group size must be higher then min. Min:$minGroupSize, Max:$maxGroupSize" }
    }
}
