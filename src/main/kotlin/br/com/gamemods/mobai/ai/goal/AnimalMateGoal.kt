package br.com.gamemods.mobai.ai.goal

import br.com.gamemods.mobai.ai.filter.TargetFilter
import br.com.gamemods.mobai.entity.smart.EntityAI
import br.com.gamemods.mobai.entity.smart.EntityProperties
import br.com.gamemods.mobai.entity.smart.SmartEntity
import br.com.gamemods.mobai.entity.smart.logic.Breedable
import br.com.gamemods.mobai.level.findClosestEntity
import br.com.gamemods.mobai.math.distanceSquared
import br.com.gamemods.mobai.math.square
import cn.nukkit.entity.Entity
import cn.nukkit.entity.impl.BaseEntity
import kotlin.reflect.KClass

open class AnimalMateGoal<E>(
    val ai: EntityAI<E>,
    var speed: Double,
    var mateClass: KClass<out Entity> = ai.entity.type.entityClass.kotlin
): Goal() where E: SmartEntity, E: BaseEntity, E: Breedable {
    protected var mate: Breedable? = null
    private var timer = 0

    protected open val validMate = TargetFilter(
        baseMaxDistance = 8.0,
        includeInvulnerable = true,
        includeTeammates = true,
        includeHidden = true,
        filter = ai.entity::canBreedWith
    )

    init {
        addControls(Control.MOVE, Control.LOOK)
    }

    override fun canStart(): Boolean {
        if (!ai.entity.isInLove) {
            return false
        }
        mate = findMate()
        return mate is EntityProperties
    }

    override fun shouldContinue(): Boolean {
        val mate = mate ?: return false
        return timer < 60
                && mate is Entity
                && mate.isAlive
                && mate.isInLove
    }

    override fun stop() {
        mate = null
        timer = 0
    }

    override fun tick() {
        val entity = ai.entity
        val mate = mate as Entity
        ai.lookControl.lookAt(mate, 10.0, entity.lookPitchSpeed)
        ai.navigation.startMovingTo(mate, speed)
        if (++timer >= 60 && entity.distanceSquared(mate) < 3.square()) {
            breed()
        }
    }

    private fun findMate(): Breedable? {
        val entity = ai.entity
        return entity.level.findClosestEntity(
            mateClass,
            validMate,
            entity,
            entity.position,
            entity.boundingBox.grow(8.0, 8.0, 8.0)
        ) as? Breedable
    }

    protected open fun breed() {
        ai.entity.breed(checkNotNull(mate))
    }
}
