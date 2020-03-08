package br.com.gamemods.mobai.ai.filter

import br.com.gamemods.mobai.entity.canSee
import br.com.gamemods.mobai.entity.canTarget
import br.com.gamemods.mobai.entity.isTeammate
import br.com.gamemods.mobai.entity.properties
import br.com.gamemods.mobai.math.MobAiMath.square
import cn.nukkit.entity.Entity
import cn.nukkit.entity.impl.BaseEntity
import cn.nukkit.player.Player

data class TargetFilter(
    val baseMaxDistance: Double = -1.0,
    val includeInvulnerable: Boolean = false,
    val includeTeammates: Boolean = false,
    val includeHidden: Boolean = false,
    val ignoreEntityTargetRules: Boolean = false,
    val useDistanceScalingFactor: Boolean = true,
    val filter: ((Entity) -> Boolean)? = null
) {
    fun test(baseEntity: Entity?, targetEntity: Entity) = when  {
        baseEntity == targetEntity -> false
        !targetEntity.isAlive -> false
        targetEntity.isClosed -> false
        targetEntity is Player && targetEntity.gamemode == Player.SPECTATOR -> false
        !includeInvulnerable && targetEntity is BaseEntity && targetEntity.invulnerable -> false
        filter?.invoke(targetEntity) == false -> false
        baseEntity == null -> true
        !ignoreEntityTargetRules && !baseEntity.canTarget(targetEntity) -> false
        !includeTeammates && baseEntity.isTeammate(targetEntity) -> false
        baseMaxDistance > 0.0 -> {
            val factor = if (useDistanceScalingFactor) targetEntity.properties.attackDistanceScalingFactor else 1.0
            val maxDistance = baseMaxDistance * factor
            val squaredDistance = baseEntity.position.distanceSquared(targetEntity.position)
            squaredDistance <= square(maxDistance)
        }
        !includeHidden && !baseEntity.canSee(targetEntity) -> false
        else -> true
    }

    companion object {
        val DEFAULT = TargetFilter()

        val INVULNERABLE = { e: Entity -> when {
            !e.isAlive -> true
            e is BaseEntity && e.invulnerable -> true
            e is Player && (e.isSpectator || e.isCreative) -> true
            else -> false
        } }
    }
}
