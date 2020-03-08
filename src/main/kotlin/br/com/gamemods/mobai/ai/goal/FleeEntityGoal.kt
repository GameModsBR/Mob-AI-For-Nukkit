package br.com.gamemods.mobai.ai.goal

import br.com.gamemods.mobai.ai.filter.TargetFilter
import br.com.gamemods.mobai.ai.pathing.Path
import br.com.gamemods.mobai.ai.pathing.TargetFinder
import br.com.gamemods.mobai.entity.smart.EntityAI
import br.com.gamemods.mobai.entity.smart.SmartEntity
import br.com.gamemods.mobai.level.findClosestEntity
import cn.nukkit.entity.Entity
import cn.nukkit.entity.impl.BaseEntity
import kotlin.reflect.KClass

class FleeEntityGoal<E>(
    val ai: EntityAI<E>,
    val fleeFromType: KClass<out Entity>,
    val distance: Float,
    val slowSpeed: Double,
    val fastSpeed: Double,
    val extraInclusionSelector: (Entity) -> Boolean = TargetFilter.AWAYS_TRUE,
    val inclusionSelector: (Entity) -> Boolean = TargetFilter.NOT_CREATIVE_SPECTATOR_PLAYER
) : Goal() where E: BaseEntity, E: SmartEntity {
    val withinRange = TargetFilter(baseMaxDistance = distance.toDouble(), filter = { inclusionSelector(it) && extraInclusionSelector(it) })
    protected var targetEntity: Entity? = null
    protected var fleePath: Path? = null
    init {
        addControls(Control.MOVE)
    }

    override fun canStart(): Boolean {
        val entity = ai.entity
        targetEntity = entity.level.findClosestEntity(
            fleeFromType,
            withinRange,
            entity,
            entity.position,
            entity.boundingBox.grow(distance.toDouble(), 3.0, distance.toDouble())
        )
        val targetEntity = targetEntity ?: return false
        val targetEntityPos = targetEntity.position
        val targetPos = TargetFinder.findTargetAwayFrom(
            ai,
            16,
            7,
            targetEntityPos.asVector3i()
        ) ?: return false
        if (targetEntityPos.distanceSquared(targetPos) < entity.distanceSquared(targetEntityPos)) {
            return false
        }
        fleePath = ai.navigation.findPathTo(targetPos, 0)
        return fleePath != null
    }

    override fun shouldContinue(): Boolean {
        return ai.navigation.isActive
    }

    override fun start() {
        ai.navigation.startMovingAlong(fleePath, slowSpeed)
    }

    override fun stop() {
        targetEntity = null
    }

    override fun tick() {
        val entity = ai.entity
        if (entity.distanceSquared(checkNotNull(targetEntity).position) < 49.0) {
            ai.navigation.speed = fastSpeed
        } else {
            ai.navigation.speed = slowSpeed
        }
    }
}
