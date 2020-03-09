package br.com.gamemods.mobai.ai.pathing

import br.com.gamemods.mobai.entity.smart.EntityAI
import br.com.gamemods.mobai.entity.smart.SmartEntity
import br.com.gamemods.mobai.level.height
import br.com.gamemods.mobai.level.isFlooded
import br.com.gamemods.mobai.math.MobAiMath
import br.com.gamemods.mobai.math.intFloor
import br.com.gamemods.mobai.math.isWithinDistance
import cn.nukkit.entity.impl.BaseEntity
import cn.nukkit.level.BlockPosition
import cn.nukkit.math.MathHelper
import cn.nukkit.math.Vector3i
import java.util.*
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

object TargetFinder {
    fun <T> findTarget(ai: EntityAI<T>, maxHorizontalDistance: Int, maxVerticalDistance: Int): Vector3i?
    where T: SmartEntity, T: BaseEntity {
        return findTarget(
            ai,
            maxHorizontalDistance,
            maxVerticalDistance,
            0,
            null,
            true,
            Math.PI / 2,
            ai.entity::pathFindingFavor,
            false,
            0,
            0,
            true
        )
    }

    fun <T> findGroundTarget(
        ai: EntityAI<T>,
        maxHorizontalDistance: Int,
        maxVerticalDistance: Int,
        preferredYDifference: Int,
        preferredAngle: Vector3i?,
        maxAngleDifference: Double
    ): Vector3i? where T: SmartEntity, T: BaseEntity {
        return findTarget(
            ai,
            maxHorizontalDistance,
            maxVerticalDistance,
            preferredYDifference,
            preferredAngle,
            true,
            maxAngleDifference,
            ai.entity::pathFindingFavor,
            true,
            0,
            0,
            false
        )
    }

    fun <T> findGroundTarget(ai: EntityAI<T>, maxHorizontalDistance: Int, maxVerticalDistance: Int): Vector3i?
            where T: SmartEntity, T: BaseEntity{
        return findGroundTarget(ai, maxHorizontalDistance, maxVerticalDistance, ai.entity::pathFindingFavor)
    }

    fun <T> findGroundTarget(
        ai: EntityAI<T>,
        maxHorizontalDistance: Int,
        maxVerticalDistance: Int,
        pathFindingFavor: (BlockPosition) -> Float
    ): Vector3i? where T: SmartEntity, T: BaseEntity {
        return findTarget(
            ai,
            maxHorizontalDistance,
            maxVerticalDistance,
            0,
            null,
            false,
            0.0,
            pathFindingFavor,
            true,
            0,
            0,
            true
        )
    }

    fun <T> findAirTarget(
        ai: EntityAI<T>,
        maxHorizontalDistance: Int,
        maxVerticalDistance: Int,
        preferredAngle: Vector3i?,
        maxAngleDifference: Float,
        distanceAboveGroundRange: Int,
        minDistanceAboveGround: Int
    ): Vector3i? where T: SmartEntity, T: BaseEntity {
        return findTarget(
            ai,
            maxHorizontalDistance,
            maxVerticalDistance,
            0,
            preferredAngle,
            false,
            maxAngleDifference.toDouble(),
            ai.entity::pathFindingFavor,
            true,
            distanceAboveGroundRange,
            minDistanceAboveGround,
            true
        )
    }

    fun <T> findTargetTowards(
        ai: EntityAI<T>,
        maxHorizontalDistance: Int,
        maxVerticalDistance: Int,
        pos: Vector3i
    ): Vector3i? where T: SmartEntity, T: BaseEntity {
        val blockPos = pos.subtract(ai.entity.asVector3i())
        return findTarget(
            ai,
            maxHorizontalDistance,
            maxVerticalDistance,
            0,
            blockPos,
            true,
            Math.PI / 2,
            ai.entity::pathFindingFavor,
            false,
            0,
            0,
            true
        )
    }

    fun <T> findTargetTowards(
        ai: EntityAI<T>,
        maxHorizontalDistance: Int,
        maxVerticalDistance: Int,
        pos: Vector3i,
        maxAngleDifference: Double
    ): Vector3i? where T: SmartEntity, T: BaseEntity {
        val blockPos = pos.subtract(ai.entity.asVector3i())
        return findTarget(
            ai,
            maxHorizontalDistance,
            maxVerticalDistance,
            0,
            blockPos,
            true,
            maxAngleDifference,
            ai.entity::pathFindingFavor,
            false,
            0,
            0,
            true
        )
    }

    fun <T> findGroundTargetTowards(
        ai: EntityAI<T>,
        maxHorizontalDistance: Int,
        maxVerticalDistance: Int,
        preferredYDifference: Int,
        pos: Vector3i,
        maxAngleDifference: Double
    ): Vector3i? where T: SmartEntity, T: BaseEntity {
        val blockPos: Vector3i = pos.subtract(ai.entity.asVector3i())
        return findTarget(
            ai,
            maxHorizontalDistance,
            maxVerticalDistance,
            preferredYDifference,
            blockPos,
            false,
            maxAngleDifference,
            ai.entity::pathFindingFavor,
            true,
            0,
            0,
            false
        )
    }

    fun <T> findTargetAwayFrom(
        ai: EntityAI<T>,
        maxHorizontalDistance: Int,
        maxVerticalDistance: Int,
        pos: Vector3i
    ): Vector3i? where T: SmartEntity, T: BaseEntity {
        val blockPos = ai.entity.asVector3i().subtract(pos)
        return findTarget(
            ai,
            maxHorizontalDistance,
            maxVerticalDistance,
            0,
            blockPos,
            true,
            Math.PI / 2,
            ai.entity::pathFindingFavor,
            false,
            0,
            0,
            true
        )
    }

    fun <T> findGroundTargetAwayFrom(
        ai: EntityAI<T>,
        maxHorizontalDistance: Int,
        maxVerticalDistance: Int,
        pos: Vector3i?
    ): Vector3i? where T: SmartEntity, T: BaseEntity {
        val blockPos = ai.entity.asVector3i().subtract(pos)
        return findTarget(
            ai,
            maxHorizontalDistance,
            maxVerticalDistance,
            0,
            blockPos,
            false,
            Math.PI / 2,
            ai.entity::pathFindingFavor,
            true,
            0,
            0,
            true
        )
    }

    private fun <T> findTarget(
        ai: EntityAI<T>,
        maxHorizontalDistance: Int,
        maxVerticalDistance: Int,
        preferredYDifference: Int,
        preferredAngle: Vector3i?,
        notInWater: Boolean,
        maxAngleDifference: Double,
        favorProvider: (BlockPosition) -> Float,
        aboveGround: Boolean,
        distanceAboveGroundRange: Int,
        minDistanceAboveGround: Int,
        validPositionsOnly: Boolean
    ): Vector3i? where T: SmartEntity, T: BaseEntity {
        val entityNavigation = ai.navigation
        val random = ai.entity.random
        val withinDistance = if (ai.hasPositionTarget) {
            ai.positionTarget.asVector3f().isWithinDistance(
                ai.entity,
                ai.positionTargetRange + maxHorizontalDistance + 1.0
            )
        } else {
            false
        }
        var found = false
        var maxFavor = Float.NEGATIVE_INFINITY
        var blockPos = ai.entity.asVector3i()

        for (i in 0..9) {
            val blockPos2 = getRandomOffset(
                random,
                maxHorizontalDistance,
                maxVerticalDistance,
                preferredYDifference,
                preferredAngle,
                maxAngleDifference
            )

            if (blockPos2 != null) {
                var x = blockPos2.x
                val y = blockPos2.y
                var z = blockPos2.z
                var blockPos4: Vector3i
                if (ai.hasPositionTarget && maxHorizontalDistance > 1) {
                    blockPos4 = ai.positionTarget
                    if (ai.entity.x > blockPos4.x) {
                        x -= random.nextInt(maxHorizontalDistance / 2)
                    } else {
                        x += random.nextInt(maxHorizontalDistance / 2)
                    }
                    if (ai.entity.z > blockPos4.z) {
                        z -= random.nextInt(maxHorizontalDistance / 2)
                    } else {
                        z += random.nextInt(maxHorizontalDistance / 2)
                    }
                }
                blockPos4 = ai.entity.add(x.toDouble(), y.toDouble(), z.toDouble()).asVector3i()
                val level = ai.entity.level
                val levelHeight = level.height
                if (blockPos4.y in 0..levelHeight
                    && (!withinDistance || ai.isInWalkTargetRange(blockPos4))
                    && (!validPositionsOnly || entityNavigation.isValidPosition(blockPos4))
                ) {
                    if (aboveGround) {
                        blockPos4 = findValidPositionAbove(blockPos4,
                            random.nextInt(distanceAboveGroundRange + 1) + minDistanceAboveGround,
                            levelHeight
                        ) {
                            level.getBlock(it).isSolid
                        }
                    }
                    if (notInWater || !level.isFlooded(blockPos4)) {
                        val pathNodeType: PathNodeType = LandPathNodeMaker.nodeType(
                            level,
                            blockPos4,
                            ai.entity
                        )
                        if (ai.entity.pathFindingPenalty(pathNodeType) == 0.0f) {
                            val favor = favorProvider(BlockPosition.from(blockPos4, level))
                            if (favor > maxFavor) {
                                maxFavor = favor
                                blockPos = blockPos4
                                found = true
                            }
                        }
                    }
                }
            }
        }
        if (found) {
            return blockPos
        }
        return null
    }

    private fun getRandomOffset(
        random: Random,
        maxHorizontalDistance: Int,
        maxVerticalDistance: Int,
        preferredYDifference: Int,
        preferredAngle: Vector3i?,
        maxAngleDifference: Double
    ): Vector3i? {
        if (preferredAngle != null && maxAngleDifference < Math.PI) {
            val atan = atan2(preferredAngle.z.toDouble(), preferredAngle.x.toDouble()) - Math.PI / 2
            val angle = atan + (2.0f * random.nextFloat() - 1.0f) * maxAngleDifference
            val f = sqrt(random.nextDouble()) * MobAiMath.SQUARE_ROOT_OF_TWO_D * maxHorizontalDistance
            val x = -f * MathHelper.sin(angle)
            val z = f * MathHelper.cos(angle)
            if (abs(x) > maxHorizontalDistance.toDouble() || abs(z) > maxHorizontalDistance.toDouble()) {
                return null
            }

            val y = random.nextInt(2 * maxVerticalDistance + 1) - maxVerticalDistance + preferredYDifference
            return Vector3i(x.intFloor(), y, z.intFloor())
        }

        val x = random.nextInt(2 * maxHorizontalDistance + 1) - maxHorizontalDistance
        val y = random.nextInt(2 * maxVerticalDistance + 1) - maxVerticalDistance + preferredYDifference
        val z = random.nextInt(2 * maxHorizontalDistance + 1) - maxHorizontalDistance
        return Vector3i(x, y, z)
    }

    fun findValidPositionAbove(
        pos: Vector3i,
        minDistanceAboveIllegal: Int,
        maxOffset: Int,
        isIllegal: (Vector3i) -> Boolean
    ): Vector3i {
        if (minDistanceAboveIllegal < 0) {
            throw IllegalArgumentException("aboveSolidAmount was $minDistanceAboveIllegal, expected >= 0")
        }

        if (!isIllegal(pos)) {
            return pos
        }

        var blockPos: Vector3i
        blockPos = pos.up()
        while (blockPos.y < maxOffset && isIllegal(blockPos)) {
            blockPos = blockPos.up()
        }
        var blockPos2: Vector3i
        var blockPos3: Vector3i
        blockPos2 = blockPos
        while (blockPos2.y < maxOffset && blockPos2.y - blockPos.y < minDistanceAboveIllegal) {
            blockPos3 = blockPos2.up()
            if (isIllegal(blockPos3)) {
                break
            }
            blockPos2 = blockPos3
        }
        return blockPos2
    }
}
