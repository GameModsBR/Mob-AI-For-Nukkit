package br.com.gamemods.mobai.entity.smart.logic

import br.com.gamemods.mobai.entity.isDeadOrImmobile
import br.com.gamemods.mobai.entity.isInLava
import br.com.gamemods.mobai.entity.isTouchingWater
import br.com.gamemods.mobai.entity.waterHeight
import cn.nukkit.math.Vector3f
import kotlin.math.abs

interface UpdateLogic: MoveLogic {
    fun onUpdate(currentTick: Int): Boolean {
        val entity = base
        if (entity.closed) {
            return false
        }

        if (!entity.isAlive) {
            /*entity.deadTicks++
            if (entity.deadTicks >= 10) {
                entity.despawnFromAll()
                entity.close()
            }
            return entity.deadTicks < 10*/
            return false
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
                updateAttacker() or
                tickMovement(tickDiff) or
                pickupLootTick(tickDiff) or
                updateDespawnCounterTick(tickDiff) or
                finalTick(tickDiff)

        entity.updateMovement()
        entity.updateData()
        return needUpdate
    }

    fun pickupLootTick(tickDiff: Int): Boolean {
        //TODO MobEntity.tickMovement()
        return false
    }

    fun finalTick(tickDiff: Int): Boolean {
        //TODO
        return false
    }

    fun updateDespawnCounterTick(tickDiff: Int): Boolean {
        return false
    }

    fun updateAttacker(): Boolean {
        val attacker = attacker ?: return false
        if (!attacker.isAlive || base.ticksLived - lastAttackedTime > 100) {
            this.attacker = null
            return false
        }
        return true
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
            val waterHeight: Double
            val bl = if (entity.isTouchingWater) {
                waterHeight = entity.waterHeight
                waterHeight > 0.0
            } else {
                waterHeight = 0.0
                false
            }

            if (bl && (!entity.isOnGround || waterHeight > 0.4)) {
                swimUpWater()
            } else if (entity.isInLava) {
                swimUpLava()
            } else if ((entity.isOnGround || bl && waterHeight <= 0.4) && jumpingCooldown == 0) {
                jump()
                jumpingCooldown = 10
            }
        } else {
            jumpingCooldown = 0
        }

        sidewaysSpeed *= 0.98F
        forwardSpeed *= 0.98F
        // TODO: Skipping initAi, which is actually elytra flying
        //val box = entity.boundingBox.clone()
        travel(Vector3f(sidewaysSpeed.toDouble(), upwardSpeed.toDouble(), forwardSpeed.toDouble()))
        //TODO Remove this debug code
        /*if ((ai.navigation.currentTarget?.distanceSquared(base) ?: 0.0) > 20.square()) {
            ai.navigation.stop()
            base.kill()
        }*/
        //TODO: Skipping push
        //TODO: Skipping tickCramming

        return needsUpdate
    }

    fun mobTick(tickDiff: Int): Boolean {
        return false
    }
}
