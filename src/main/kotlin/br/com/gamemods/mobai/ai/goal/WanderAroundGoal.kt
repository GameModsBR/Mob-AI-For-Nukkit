package br.com.gamemods.mobai.ai.goal

import br.com.gamemods.mobai.ai.goal.Goal.Control.MOVE
import br.com.gamemods.mobai.ai.pathing.TargetFinder
import br.com.gamemods.mobai.entity.smart.EntityAI
import br.com.gamemods.mobai.entity.smart.SmartEntity
import cn.nukkit.entity.impl.BaseEntity
import cn.nukkit.math.Vector3i

open class WanderAroundGoal<E>(
    protected val ai: EntityAI<E>,
    protected val speed: Double,
    private val chance: Int = 120,
    var ignoreChanceOnce: Boolean = false
): Goal() where E: SmartEntity, E: BaseEntity {
    private var targetBlock = Vector3i()
    init {
        addControls(MOVE)
    }

    override fun canStart(): Boolean {
        ai.entity.apply {
            if (passengers.isNotEmpty()) {
                return false
            }

            if (!ignoreChanceOnce) {
                /*if (deSpawnCounter >= 100) {
                    return false
                }*/

                if (random.nextInt(chance) != 0) {
                    return false
                }
            }

            targetBlock = findTarget() ?: return false
            ignoreChanceOnce = false
            return true
        }
    }

    protected open fun findTarget(): Vector3i? {
        return TargetFinder.findTarget(ai, 10, 7)
    }

    override fun shouldContinue(): Boolean {
        return ai.navigation.isActive && ai.entity.passengers.isEmpty()
    }

    override fun start() {
        ai.navigation.startMovingTo(targetBlock, speed)
    }

    override fun stop() {
        ai.navigation.stop()
        super.stop()
    }
}
