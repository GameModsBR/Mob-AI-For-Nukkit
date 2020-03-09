package br.com.gamemods.mobai.ai.pathing

import br.com.gamemods.mobai.entity.isTouchingWater
import br.com.gamemods.mobai.entity.smart.EntityAI
import br.com.gamemods.mobai.entity.smart.SmartEntity
import br.com.gamemods.mobai.level.getBlockIdAt
import br.com.gamemods.mobai.level.height
import br.com.gamemods.mobai.level.isAir
import br.com.gamemods.mobai.level.isSkyVisible
import br.com.gamemods.mobai.math.floor
import cn.nukkit.block.BlockIds.FLOWING_WATER
import cn.nukkit.block.BlockIds.WATER
import cn.nukkit.entity.Entity
import cn.nukkit.entity.impl.BaseEntity
import cn.nukkit.math.Vector3f
import cn.nukkit.math.Vector3i

class WalkingNavigation<E>(ai: EntityAI<E>) : EntityNavigation<E>(ai) where E: SmartEntity, E: BaseEntity {
    var avoidSunlight = false
    override val nodeMaker = LandPathNodeMaker<E>().apply {
        canEnterOpenDoors = true
    }

    override val pathNodeNavigator = PathNodeNavigator(nodeMaker, navigationRange)

    override val isAtValidPosition: Boolean
        get() = entity.let { it.vehicle != null || it.isOnGround } || isInLiquid

    override val position: Vector3f
        get() = entity.apply {
            position.apply position@ {
                if (!isTouchingWater || !nodeMaker.canSwim) {
                    y = (y + 0.5).floor()
                } else {
                    val blockPos = asVector3i()
                    var block = level.getBlockIdAt(blockPos)
                    var distance = 0

                    do {
                        if (block != WATER && block != FLOWING_WATER) {
                            y = blockPos.y.toDouble()
                            return@position
                        }
                        blockPos.y++
                        block = level.getBlockIdAt(blockPos)
                        ++distance
                    } while (distance <= 16)

                    y = y.floor()
                }
            }
        }

    override fun findPathTo(target: Vector3i, distance: Int): Path? {
        var blockPos2: Vector3i
        val level = entity.level
        var adjusted = target
        if (level.getBlock(target).isAir) {
            blockPos2 = target.down()
            while (blockPos2.y > 0 && level.getBlock(blockPos2).isAir) {
                blockPos2 = blockPos2.down()
            }
            if (blockPos2.y > 0) {
                return super.findPathTo(blockPos2.up(), distance)
            }
            while (blockPos2.y < level.height && level.getBlock(blockPos2).isAir) {
                blockPos2 = blockPos2.up()
            }
            adjusted = blockPos2
        }

        return if (!level.getBlock(adjusted).isSolid) {
            super.findPathTo(target, distance)
        } else {
            blockPos2 = target.up()
            while (blockPos2.y < level.height && level.getBlock(blockPos2).isSolid) {
                blockPos2 = blockPos2.up()
            }
            super.findPathTo(blockPos2, distance)
        }
    }

    override fun findPathTo(entity: Entity, distance: Int): Path? {
        return findPathTo(entity.position.asVector3i(), distance)
    }

    override fun validatePath(path: Path) {
        super.validatePath(path)
        if (avoidSunlight) {
            val level = entity.level
            if (level.isSkyVisible(entity.position.apply { y += 0.5 })) {
                return
            }

            path.forEachIndexed { i, pathNode ->
                if (level.canBlockSeeSky(pathNode)) {
                    path.setLength(i)
                    return
                }
            }
        }
    }
}
