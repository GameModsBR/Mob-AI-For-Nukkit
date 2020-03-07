package br.com.gamemods.mobai.ai.goal

import br.com.gamemods.mobai.ai.filter.TargetFilter
import br.com.gamemods.mobai.entity.smart.EntityAI
import br.com.gamemods.mobai.entity.smart.SmartEntity
import br.com.gamemods.mobai.level.findClosestEntity
import br.com.gamemods.mobai.level.findClosestPlayer
import cn.nukkit.entity.Entity
import cn.nukkit.entity.impl.BaseEntity
import cn.nukkit.math.AxisAlignedBB
import cn.nukkit.player.Player
import kotlin.reflect.KClass

open class FollowTargetGoal<E, T: Entity>(
    ai: EntityAI<E>,
    protected val targetClass: KClass<T>,
    checkVisibility: Boolean,
    checkNavigable: Boolean = false,
    protected val reciprocalChance: Int = 10,
    private val filter: ((Entity) -> Boolean)? = null
) : TrackTargetGoal<E>(ai, checkVisibility, checkNavigable) where E: SmartEntity, E: BaseEntity {
    protected var targetEntity: Entity? = null
    protected open val targetFilter by lazy {
        TargetFilter(baseMaxDistance = followRange.toDouble(), filter = filter)
    }
    init {
        addControls(Control.TARGET)
    }

    override fun canStart(): Boolean {
        if (reciprocalChance > 0 && ai.entity.random.nextInt(reciprocalChance) != 0) {
            return false
        }
        findClosestTarget()
        return targetEntity != null
    }

    protected open fun getSearchBox(distance: Double): AxisAlignedBB {
        return ai.entity.boundingBox.expand(distance, 4.0, distance)
    }

    protected fun findClosestTarget() {
        val entity = ai.entity
        targetEntity = if (targetClass == Player::class) {
            entity.level.findClosestPlayer(targetFilter, entity, entity)
        } else {
            entity.level.findClosestEntity(targetClass, targetFilter, entity, entity, getSearchBox(followRange.toDouble()))
        }
    }

    override fun start() {
        ai.target = targetEntity
        super.start()
    }
}
