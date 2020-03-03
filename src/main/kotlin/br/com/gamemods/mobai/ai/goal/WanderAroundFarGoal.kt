package br.com.gamemods.mobai.ai.goal

import br.com.gamemods.mobai.ai.pathing.TargetFinder
import br.com.gamemods.mobai.entity.isInsideOfWaterOrBubbles
import br.com.gamemods.mobai.entity.smart.EntityAI
import br.com.gamemods.mobai.entity.smart.SmartEntity
import cn.nukkit.entity.impl.BaseEntity
import cn.nukkit.math.Vector3i

class WanderAroundFarGoal<E>(ai: EntityAI<E>, speed: Double, val probability: Float = 0.001F)
    : WanderAroundGoal<E>(ai, speed)
        where E: SmartEntity, E: BaseEntity {

    override fun findTarget(): Vector3i? {
        val entity = ai.entity
        if (entity.isInsideOfWaterOrBubbles) {
            return TargetFinder.findGroundTarget(ai, 15, 7)
                ?: super.findTarget()
        }
        if (entity.random.nextFloat() >= probability) {
            return TargetFinder.findGroundTarget(ai, 10, 7)
        }
        return super.findTarget()
    }
}
