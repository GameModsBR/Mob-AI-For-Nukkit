package br.com.gamemods.mobai.ai.pathing

import br.com.gamemods.mobai.entity.smart.EntityAI
import br.com.gamemods.mobai.level.ChunkCache
import cn.nukkit.entity.impl.BaseEntity
import cn.nukkit.math.Vector3i

class PathNodeNavigator {
    //TODO
    fun findPathToAny(
        level: ChunkCache,
        ai: EntityAI,
        entity: BaseEntity,
        positions: Set<Vector3i>,
        followRange: Float,
        distance: Int,
        rangeMultiplier: Float
    ): Path? {
        TODO("not implemented")
    }
}
