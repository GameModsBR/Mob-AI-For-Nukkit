package br.com.gamemods.mobai.ai.goal

import br.com.gamemods.mobai.entity.isAdult
import br.com.gamemods.mobai.entity.smart.EntityAI
import br.com.gamemods.mobai.entity.smart.SmartEntity
import br.com.gamemods.mobai.level.closestTo
import br.com.gamemods.mobai.level.findEntities
import br.com.gamemods.mobai.math.distanceSquared
import br.com.gamemods.mobai.math.square
import cn.nukkit.entity.Entity
import cn.nukkit.entity.impl.BaseEntity

open class FollowParentGoal<E>(
    val ai: EntityAI<E>,
    var speed: Double
): Goal() where E: SmartEntity, E: BaseEntity {
    private var parent: Entity? = null
    private var delay = 0

    override fun canStart(): Boolean {
        val baby = ai.entity
        if (baby.isAdult) {
            return false
        }
        val (adult, distance) = baby.level.findEntities(
            baby.type.entityClass.kotlin,
            baby.boundingBox.grow(8.0, 4.0, 8.0),
            null,
            Entity::isAdult
        ).closestTo(baby) ?: return false
        if (distance < 3.square()) {
            return false
        }
        parent = adult
        return true
    }

    override fun shouldContinue(): Boolean {
        val child = ai.entity
        val adult = parent ?: return false
        if (child.isAdult || !adult.isAlive) {
            return false
        }
        val distance = child.distanceSquared(adult)
        return distance in 3.0.square()..16.0.square()
    }

    override fun start() {
        delay = 0
    }

    override fun stop() {
        parent = null
    }

    override fun tick() {
        if (--delay > 0) {
            return
        }

        delay = 10
        ai.navigation.startMovingTo(checkNotNull(parent), speed)
    }
}
