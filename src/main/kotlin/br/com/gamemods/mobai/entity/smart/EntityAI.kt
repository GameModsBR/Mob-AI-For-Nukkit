package br.com.gamemods.mobai.entity.smart

import br.com.gamemods.mobai.ai.control.JumpControl
import br.com.gamemods.mobai.ai.control.LookControl
import br.com.gamemods.mobai.ai.control.MoveControl
import br.com.gamemods.mobai.ai.goal.GoalSelector
import br.com.gamemods.mobai.ai.pathing.EntityNavigation
import br.com.gamemods.mobai.ai.pathing.WalkingNavigation
import br.com.gamemods.mobai.inventory.EntityEquipments
import br.com.gamemods.mobai.math.square
import cn.nukkit.entity.Entity
import cn.nukkit.entity.impl.BaseEntity
import cn.nukkit.math.Vector3i

class EntityAI<E> (
    val entity: E,
    lookControlFactory: (EntityAI<E>) -> LookControl<E> = ::LookControl,
    navigationFactory: (EntityAI<E>) -> EntityNavigation<E> = ::WalkingNavigation,
    moveControlFactory: (EntityAI<E>) -> MoveControl<E> = ::MoveControl,
    jumpControlFactory: (EntityAI<E>) -> JumpControl<E> = ::JumpControl,
    val equipments: EntityEquipments = EntityEquipments(entity)
) where E: BaseEntity, E: SmartEntity {

    val goalSelector = GoalSelector()
    val targetSelector = GoalSelector()

    val lookControl = lookControlFactory(this)
    val navigation = navigationFactory(this)
    val moveControl = moveControlFactory(this)
    val jumpControl = jumpControlFactory(this)

    var target: Entity? = null
    var positionTarget = Vector3i()
    var positionTargetRange = -1F

    val hasPositionTarget get() = positionTargetRange != -1F

    fun tickAI(tickDiff: Int): Boolean {
        entity.despawnCounter++
        entity.visibleEntityIdsCache.clear()
        entity.invisibleEntityIdsCache.clear()
        val shouldUpdate = targetSelector.tick() or
                goalSelector.tick() or
                navigation.tick() or
                entity.mobTick(tickDiff)

        if (entity.isClosed) {
            return false
        }

        return shouldUpdate or
                moveControl.tick() or
                lookControl.tick() or
                jumpControl.tick()
    }

    fun isInWalkTargetRange(pos: Vector3i): Boolean {
        return if (positionTargetRange == -1.0f) {
            true
        } else {
            positionTarget.distanceSquared(pos) < positionTargetRange.square()
        }
    }
}
