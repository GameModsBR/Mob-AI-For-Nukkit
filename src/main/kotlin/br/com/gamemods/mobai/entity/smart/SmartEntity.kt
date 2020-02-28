package br.com.gamemods.mobai.entity.smart

import cn.nukkit.entity.Entity
import cn.nukkit.entity.EntityDamageable

interface SmartEntity: Entity {
    val ai: EntityAI

    val attackDistanceScalingFactor get() = 1.0

    fun updateMovement() = ai.updateMovementInclHead()
    override fun onUpdate(currentTick: Int) = ai.onSmartEntityUpdate(currentTick)

    fun canTarget(entity: Entity) = entity is EntityDamageable

    fun isTeammate(entity: Entity) = false

    fun canSee(entity: Entity): Boolean {
        //TODO
        return true
    }
}
