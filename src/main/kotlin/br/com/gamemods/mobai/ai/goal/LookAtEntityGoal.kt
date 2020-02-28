package br.com.gamemods.mobai.ai.goal

import br.com.gamemods.mobai.ai.filter.EntityFilters
import br.com.gamemods.mobai.ai.filter.TargetFilter
import br.com.gamemods.mobai.ai.goal.Goal.Control.LOOK
import br.com.gamemods.mobai.entity.eyePosition
import br.com.gamemods.mobai.entity.smart.EntityAI
import br.com.gamemods.mobai.level.findClosestEntity
import br.com.gamemods.mobai.level.findClosestPlayer
import br.com.gamemods.mobai.math.MobAiMath.square
import cn.nukkit.entity.Entity
import cn.nukkit.player.Player
import kotlin.reflect.KClass

class LookAtEntityGoal(
    private val ai: EntityAI,
    private val type: KClass<out Entity>,
    private val range: Double,
    private val chance: Float = 0.02F
): Goal() {
    private var target: Entity? = null
    private var lookTime = 0
    private val targetFilter = TargetFilter(
        baseMaxDistance = range,
        includeTeammates = true,
        includeInvulnerable = true,
        ignoreEntityTargetRules = true,
        filter = if (type == Player::class) EntityFilters.doesNotRides(ai.entity) else null
    )
    init {
        controls += LOOK
    }

    override fun canStart(): Boolean {
        if (ai.random.nextFloat() >= chance) {
            return false
        }

        ai.target?.let {
            target = it
            return true
        }

        val entity = ai.entity
        target = if (type == Player::class) {
            entity.level.findClosestPlayer(targetFilter, entity, entity.position)
        } else {
            entity.level.findClosestEntity(type, targetFilter, entity, entity.position, entity.getBoundingBox().expand(range, 3.0, range))
        }

        return target != null
    }

    override fun shouldContinue() = target?.let { target ->
        target.isAlive
                && lookTime > 0
                && ai.entity.distanceSquared(target.position) <= square(range)
    } ?: false

    override fun start() {
        lookTime = 40 + ai.random.nextInt(40)
    }

    override fun stop() {
        target = null
    }

    override fun tick() {
        target?.let { ai.lookControl.lookAt(it.eyePosition) }
        lookTime--
    }
}
