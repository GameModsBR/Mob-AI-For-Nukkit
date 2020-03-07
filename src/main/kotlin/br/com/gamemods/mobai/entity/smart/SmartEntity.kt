package br.com.gamemods.mobai.entity.smart

import br.com.gamemods.mobai.entity.isDeadOrImmobile
import br.com.gamemods.mobai.entity.isInLava
import br.com.gamemods.mobai.entity.isTouchingWater
import br.com.gamemods.mobai.entity.movementSpeed
import br.com.gamemods.mobai.level.isClimbable
import br.com.gamemods.mobai.math.MobAiMath
import cn.nukkit.block.BlockLiquid
import cn.nukkit.block.BlockTrapdoor
import cn.nukkit.entity.Attribute.*
import cn.nukkit.entity.Entity
import cn.nukkit.entity.EntityDamageable
import cn.nukkit.entity.impl.BaseEntity
import cn.nukkit.level.BlockPosition
import cn.nukkit.math.Vector3f
import kotlin.math.abs

interface SmartEntity: EntityProperties, MoveLogic {
    val ai: EntityAI<*>
    val equipments get() = ai.equipments

    private inline val entity get() = this as Entity
    private inline val base get() = this as BaseEntity

    @Suppress("RedundantIf")
    val isClimbing: Boolean get() { base.apply {
        val blockPos = position.asVector3i()
        val block = level.getBlock(blockPos)
        return if (block.isClimbable) {
            //climbing = blockPos
            true
        } else if (block is BlockTrapdoor && canEnterTrapdoor(block)) {
            //climbing = blockPos
            true
        } else {
            false
        }
    }}

    fun initAttributes() {
        entity.movementSpeed = 0F
        addAttributes(MAX_HEALTH, KNOCKBACK_RESISTANCE, MOVEMENT_SPEED, FOLLOW_RANGE)
    }

    fun onUpdate(currentTick: Int): Boolean {
        val entity = base
        if (entity.closed) {
            return false
        }

        if (!entity.isAlive) {
            entity.deadTicks++
            if (entity.deadTicks >= 10) {
                entity.despawnFromAll()
                entity.close()
            }
            return entity.deadTicks < 10
        }

        entity.apply {
            if (justCreated && (!onGround || motionY != 0.0)) {
                val bb = boundingBox.clone()
                bb.minY = bb.minY - 0.75
                onGround = level.getCollisionBlocks(bb).isNotEmpty()
            }
        }

        val tickDiff = currentTick - entity.lastUpdate

        if (tickDiff <= 0) {
            return false
        }

        entity.lastUpdate = currentTick

        val needUpdate = entity.entityBaseTick(tickDiff) or
            ai.tickAI(tickDiff) or
            tickMovement(tickDiff)

        entity.updateMovement()
        entity.updateData()
        return needUpdate
    }

    fun tickMovement(tickDiff: Int): Boolean {
        var needsUpdate = true

        val entity = base
        if (jumpingCooldown > 0) {
            jumpingCooldown--
        }

        if (isAiDisabled) {
            entity.motion = entity.motion.multiply(0.98)
        }

        val motion = entity.motion
        if (abs(motion.x) < 0.003) {
            motion.x = 0.0
        }
        if (abs(motion.y) < 0.003) {
            motion.y = 0.0
        }
        if (abs(motion.z) < 0.003) {
            motion.z = 0.0
        }

        entity.motion = motion
        if (entity.isDeadOrImmobile) {
            isJumping = false
            sidewaysSpeed = 0F
            forwardSpeed = 0F
        } else if (!isAiDisabled) {
            needsUpdate = needsUpdate or ai.tickAI(tickDiff)
        }

        if (isJumping) {
            val waterHeight: Float
            val bl = if (entity.isTouchingWater) {
                waterHeight = entity.levelBlock.let {
                    ((it as? BlockLiquid) ?: (it.getBlockAtLayer(1) as? BlockLiquid))?.fluidHeightPercent ?: 0F
                }
                waterHeight > 0
            } else {
                waterHeight = 0F
                false
            }

            if (!bl || entity.isOnGround && waterHeight <= 0.4) {
                if (entity.isInLava) {
                    swimUpLava()
                } else if ((entity.isOnGround || bl && waterHeight <= 0.4) && jumpingCooldown == 0) {
                    jump()
                    jumpingCooldown = 10
                }
            } else {
                swimUpWater()
            }
        } else {
            jumpingCooldown = 0
        }

        sidewaysSpeed *= 0.98F
        forwardSpeed *= 0.98F
        // TODO: Skipping initAi, which is actually elytra flying
        //val box = entity.boundingBox.clone()
        travel(Vector3f(sidewaysSpeed.toDouble(), upwardSpeed.toDouble(), forwardSpeed.toDouble()))
        //TODO: Skipping push
        //TODO: Skipping tickCramming

        return needsUpdate
    }

    fun mobTick(tickDiff: Int): Boolean {
        return false
    }

    fun swimUpLava() {

    }

    fun swimUpWater() {

    }

    fun jump() {

    }

    fun canTarget(entity: Entity) = entity is EntityDamageable

    fun isTeammate(entity: Entity) = false

    fun canSee(entity: Entity): Boolean {
        val id = entity.runtimeId
        if (id in visibleEntityIdsCache) {
            return true
        }
        if (id in invisibleEntityIdsCache) {
            return false
        }
        val canSee = canSeeUncached(entity)
        if (canSee) {
            visibleEntityIdsCache += id
        } else {
            invisibleEntityIdsCache += id
        }
        return canSee
    }

    fun canSeeUncached(entity: Entity): Boolean {
        TODO()
    }

    fun pathFindingFavor(pos: BlockPosition) = 0F

    fun setPositionAndRotation(pos: Vector3f, yaw: Double, pitch: Double, headYaw: Double): Boolean {
        if (entity.setPositionAndRotation(pos, yaw, pitch)) {
            this.headYaw = headYaw
            return true
        }
        return false
    }

    fun setRotation(yaw: Double, pitch: Double, headYaw: Double) {
        this.headYaw = headYaw
        entity.setRotation(yaw, pitch)
    }

    fun updateMovement() = updateMovementInclHead()
    fun updateMovementInclHead() {
        base.apply {
            val diffPosition = MobAiMath.square(x - lastX) + MobAiMath.square(y - lastY) + MobAiMath.square(z - lastZ)
            val diffRotation = MobAiMath.square(yaw - lastYaw) + MobAiMath.square(pitch - lastPitch)
            val diffHeadRotation = MobAiMath.square(headYaw - lastHeadYaw) + MobAiMath.square(pitch - lastPitch)

            val diffMotion =
                MobAiMath.square(motionX - lastMotionX)
            +MobAiMath.square(motionY - lastMotionY)
            +MobAiMath.square(motionZ - lastMotionZ)

            if (diffPosition > 0.0001 || diffRotation > 1.0 || diffHeadRotation > 1.0) { //0.2 ** 2, 1.5 ** 2
                lastX = x
                lastY = y
                lastZ = z
                lastYaw = yaw
                lastPitch = pitch
                lastHeadYaw = headYaw
                addMovement(x, y, z, yaw, pitch, headYaw)
            }

            if (diffMotion > 0.0025 || diffMotion > 0.0001 && motion.lengthSquared() <= 0.0001) { //0.05 ** 2
                lastMotionX = motionX
                lastMotionY = motionY
                lastMotionZ = motionZ
                addMotion(motionX, motionY, motionZ)
            }
        }
    }
}
