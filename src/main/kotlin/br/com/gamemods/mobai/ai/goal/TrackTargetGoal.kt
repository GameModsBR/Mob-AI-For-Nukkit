package br.com.gamemods.mobai.ai.goal

import br.com.gamemods.mobai.ai.filter.TargetFilter
import br.com.gamemods.mobai.entity.attribute
import br.com.gamemods.mobai.entity.smart.EntityAI
import br.com.gamemods.mobai.entity.smart.SmartEntity
import br.com.gamemods.mobai.math.square
import cn.nukkit.entity.Attribute
import cn.nukkit.entity.Entity
import cn.nukkit.entity.impl.BaseEntity
import cn.nukkit.math.MathHelper
import cn.nukkit.player.Player

abstract class TrackTargetGoal<E>(
    val ai: EntityAI<E>,
    val checkVisibility: Boolean,
    val checkNavigable: Boolean = false
): Goal() where E: BaseEntity, E: SmartEntity {
    private var canNavigateFlag = 0
    private var checkCanNavigateCooldown = 0
    private var timeWithoutVisibility = 0
    protected var target: Entity? = null
    var maxTimeWithoutVisibility = 60
    protected open val followRange get() = ai.entity.attribute(Attribute.FOLLOW_RANGE).value

    override fun shouldContinue(): Boolean {
        val target = ai.target ?: this.target ?: return false
        val entity = ai.entity
        if (!target.isAlive || entity.isTeammate(target)) {
            return false
        }

        val followRange = followRange
        if (entity.distanceSquared(target.position) > followRange.square()) {
            return false
        }

        if (checkVisibility) {
            if (entity.canSee(target)) {
                timeWithoutVisibility = 0
            } else if (++timeWithoutVisibility > maxTimeWithoutVisibility) {
                return false
            }
        }

        if (target is Player && target.invulnerable) {
            return false
        }

        ai.target = target

        return true
    }

    override fun start() {
        canNavigateFlag = 0
        checkCanNavigateCooldown = 0
        timeWithoutVisibility = 0
    }

    override fun stop() {
        ai.target = null
    }

    protected open fun canTrack(target: Entity?, filter: TargetFilter): Boolean {
        if (target == null || !filter.test(ai.entity, target) || !ai.isInWalkTargetRange(target.position.asVector3i())) {
            return false
        }
        if (checkNavigable) {
            if (--checkCanNavigateCooldown <= 0) {
                canNavigateFlag = 0
            }
            if (canNavigateFlag == 0) {
                canNavigateFlag = if (canNavigateToEntity(target)) 1 else 2
            }
            if (canNavigateFlag == 2) {
                return false
            }
        }
        return true
    }

    private fun canNavigateToEntity(entity: Entity): Boolean {
        checkCanNavigateCooldown = 10 + ai.entity.random.nextInt(5)
        val path = ai.navigation.findPathTo(entity, 0) ?: return false
        val end = path.end ?: return false
        val x = end.x - MathHelper.floor(entity.x)
        val z = end.z - MathHelper.floor(entity.z)
        val distanceSquared = x.square() + z.square()
        return distanceSquared <= 2.25
    }
}
