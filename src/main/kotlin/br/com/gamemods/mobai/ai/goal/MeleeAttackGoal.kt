package br.com.gamemods.mobai.ai.goal

import br.com.gamemods.mobai.ai.pathing.Path
import br.com.gamemods.mobai.entity.smart.EntityAI
import br.com.gamemods.mobai.entity.smart.SmartEntity
import br.com.gamemods.mobai.math.square
import cn.nukkit.entity.Entity
import cn.nukkit.entity.impl.BaseEntity
import cn.nukkit.math.Vector3f
import cn.nukkit.player.Player

open class MeleeAttackGoal<E>(val ai: EntityAI<E>, val speed: Double, val pauseWhenIdle: Boolean): Goal() where E: BaseEntity, E: SmartEntity{
    protected var ticksUntilAttack = 0
    private var path: Path? = null
    private var updateCountdownTicks = 0
    private var targetPos: Vector3f? = null
    private var lastUpdateTime = 0
    init {
        addControls(Control.MOVE, Control.LOOK)
    }

    override fun canStart(): Boolean {
        val time = ai.entity.ticksLived
        if (time - lastUpdateTime < 20) {
            return false
        }
        lastUpdateTime = time

        val target = ai.target ?: return false
        if (!target.isAlive) {
            return false
        }
        path = ai.navigation.findPathTo(target, 0)
        if (path != null) {
            return true
        }
        return getSquaredMaxAttackDistance(target) >= ai.entity.distanceSquared(target.position)
    }

    override fun shouldContinue(): Boolean {
        val target = ai.target ?: return false
        return when {
            !target.isAlive -> false
            !pauseWhenIdle -> ai.navigation.isActive
            !ai.isInWalkTargetRange(target.position.asVector3i()) -> false
            target is Player && (target.isSpectator || target.isCreative) -> false
            else -> true
        }
    }

    override fun start() {
        ai.navigation.startMovingAlong(path, speed)
        ai.entity.attacking = ai.target
        updateCountdownTicks = 0
    }

    override fun stop() {
        ai.target = null
        ai.entity.attacking = null
        ai.navigation.stop()
    }

    override fun tick() {
        val target = checkNotNull(ai.target)
        ai.lookControl.lookAt(target, 30.0, 30.0)
        val entity = ai.entity
        val currentPos = target.position
        val distanceSquared = entity.distanceSquared(currentPos)
        updateCountdownTicks--
        if ((pauseWhenIdle || entity.canSee(target))
            && updateCountdownTicks <= 0
            && (targetPos?.distanceSquared(currentPos) ?: Double.MAX_VALUE >= 1.0
                    || entity.random.nextFloat() < 0.05F)
        ){
            targetPos = currentPos
            updateCountdownTicks = 4 + entity.random.nextInt(7)
            if (distanceSquared > 1024.0) {
                updateCountdownTicks += 10
            } else if (distanceSquared > 256.0) {
                updateCountdownTicks += 5
            }
            if (!ai.navigation.startMovingTo(target, speed)) {
                updateCountdownTicks += 15
            }
        }
        ticksUntilAttack = (ticksUntilAttack - 1).coerceAtLeast(0)
        attack(target, distanceSquared)
    }

    fun attack(target: Entity, distanceSquared: Double) {
        val max = getSquaredMaxAttackDistance(target)
        if (distanceSquared <= max && ticksUntilAttack <= 0) {
            ticksUntilAttack = 20
            //TODO this.mob.swingHand(Hand.MAIN_HAND);
            ai.entity.tryAttack(target)
        }
    }

    protected open fun getSquaredMaxAttackDistance(target: Entity): Double {
        return (ai.entity.width * 2.0).square() + target.width
    }
}
