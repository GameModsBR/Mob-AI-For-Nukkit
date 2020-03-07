package br.com.gamemods.mobai.ai.goal

import br.com.gamemods.mobai.ai.pathing.TargetFinder
import br.com.gamemods.mobai.entity.smart.EntityAI
import br.com.gamemods.mobai.entity.smart.SmartEntity
import br.com.gamemods.mobai.level.getWaterDamage
import cn.nukkit.entity.Entity
import cn.nukkit.entity.impl.BaseEntity
import cn.nukkit.level.ChunkManager
import cn.nukkit.math.Vector3i

open class EscapeDangerGoal<E>(val ai: EntityAI<E>, var speed: Double): Goal() where E: BaseEntity, E: SmartEntity {
    protected var target: Vector3i? = null

    init {
        addControls(Control.MOVE)
    }

    override fun canStart(): Boolean {
        val entity = ai.entity
        val onFire = entity.isOnFire
        val attacker = entity.attacker
        if (!onFire && attacker == null) {
            return false
        }
        if (onFire) {
            val water = locateClosestWater(entity.level, entity, 5, 4)
            if (water != null) {
                target = water
                return true
            }
        }

        return findTarget()
    }

    protected open fun findTarget(): Boolean {
        target = TargetFinder.findTarget(ai, 5, 4) ?: return false
        return true
    }

    override fun start() {
        ai.navigation.startMovingTo(checkNotNull(target).clone(), speed)
    }

    override fun shouldContinue(): Boolean {
        return ai.navigation.isActive
    }

    protected open fun locateClosestWater(blockView: ChunkManager, entity: Entity, rangeX: Int, rangeY: Int): Vector3i? {
        val blockPos = entity.position.asVector3i()
        val x: Int = blockPos.getX()
        val y: Int = blockPos.getY()
        val z: Int = blockPos.getZ()
        var range = rangeX * rangeX * rangeY * 2.toFloat()
        var closest: Vector3i? = null
        for (scanX in x - rangeX..x + rangeX) {
            for (scanY in y - rangeY..y + rangeY) {
                for (scanZ in z - rangeX..z + rangeX) {
                    blockPos.setComponents(scanX, scanY, scanZ)
                    val water = blockView.getWaterDamage(blockPos)
                    if (water == -1) {
                        continue
                    }


                    val distance =
                        ((scanX - x) * (scanX - x) + (scanY - y) * (scanY - y) + (scanZ - z) * (scanZ - z)).toFloat()

                    if (distance >= range) {
                        continue
                    }

                    range = distance
                    closest = blockPos.clone()
                }
            }
        }
        return closest
    }
}
