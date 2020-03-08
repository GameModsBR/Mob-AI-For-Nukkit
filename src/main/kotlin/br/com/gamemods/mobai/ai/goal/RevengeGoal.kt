package br.com.gamemods.mobai.ai.goal

import br.com.gamemods.mobai.ai.filter.TargetFilter
import br.com.gamemods.mobai.entity.isTeammate
import br.com.gamemods.mobai.entity.smart.EntityAI
import br.com.gamemods.mobai.entity.smart.SmartEntity
import cn.nukkit.entity.EntityOwnable
import cn.nukkit.entity.impl.BaseEntity
import kotlin.reflect.KClass

open class RevengeGoal<E>(
    ai: EntityAI<E>,
    protected vararg val noRevenge: KClass<*>,
    protected val groupRevenge: Boolean = false,
    protected val noHelp: Array<KClass<*>> = emptyArray(),
    checkVisibility: Boolean = true,
    checkNavigable: Boolean = false
) : TrackTargetGoal<E>(ai, checkVisibility, checkNavigable) where E: BaseEntity, E: SmartEntity {
    private var lastAttackedTime = 0
    init {
        addControls(Control.TARGET)
    }

    override fun canStart(): Boolean {
        val attacker = ai.entity.attacker ?: return false
        if(ai.entity.lastAttackedTime == lastAttackedTime || noRevenge.any { it.java.isInstance(attacker) }) {
            return false
        }
        return canTrack(attacker, FILTER)
    }

    override fun start() {
        ai.target = ai.entity.attacker
        target = ai.target
        lastAttackedTime = ai.entity.lastAttackedTime
        maxTimeWithoutVisibility = 300
        if (groupRevenge) {
            callSameTypeForRevenge()
        }
        super.start()
    }

    protected open fun callSameTypeForRevenge() {
        val followRange = followRange.toDouble()
        val entity = ai.entity
        val attacker = checkNotNull(entity.attacker)
        val box = entity.boundingBox.apply {
            maxX++
            maxY++
            maxZ++
        }.expand(followRange, 10.0, followRange)
        entity.level.getNearbyEntities(box, entity).asSequence()
            .filter { it.type == entity.type }
            .filterNot(attacker::isTeammate)
            .filter { entity !is EntityOwnable || it is EntityOwnable && entity.owner != it.owner }
            .filter { noHelp.none { type -> type.java.isInstance(it) } }
            .mapNotNull { it as? SmartEntity }
            .filter { it.ai.target == null }
            .forEach { it.ai.target = attacker }
    }

    companion object {
        protected val FILTER = TargetFilter(includeHidden = true, useDistanceScalingFactor = false)
    }
}
