package br.com.gamemods.mobai.entity

import br.com.gamemods.mobai.entity.smart.SmartEntity
import cn.nukkit.entity.Entity
import cn.nukkit.entity.EntityDamageable
import cn.nukkit.level.Position

val Entity.eyePosition get() = Position(x, y + eyeHeight, z, level)
val Entity.attackDistanceScalingFactor get() = (this as? SmartEntity)?.attackDistanceScalingFactor ?: 1.0
fun Entity.canTarget(entity: Entity) = (this as? SmartEntity)?.canTarget(entity) ?: entity is EntityDamageable
fun Entity.isTeammate(entity: Entity) = (this as? SmartEntity)?.isTeammate(entity) ?: false
fun Entity.canSee(entity: Entity) = (this as? SmartEntity)?.canSee(entity) ?: false

tailrec fun Entity.isRiding(entity: Entity): Boolean {
    val vehicle = vehicle ?: return false
    if (vehicle == entity) {
        return true
    }
    return vehicle.isRiding(entity)
}
