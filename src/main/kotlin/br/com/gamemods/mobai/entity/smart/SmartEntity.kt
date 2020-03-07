package br.com.gamemods.mobai.entity.smart

import br.com.gamemods.mobai.entity.*
import br.com.gamemods.mobai.level.get
import br.com.gamemods.mobai.level.isClimbable
import br.com.gamemods.mobai.level.jumpVelocityMultiplier
import br.com.gamemods.mobai.math.MobAiMath
import br.com.gamemods.mobai.math.square
import cn.nukkit.block.BlockLiquid
import cn.nukkit.block.BlockTrapdoor
import cn.nukkit.entity.Attribute.*
import cn.nukkit.entity.Entity
import cn.nukkit.entity.Projectile
import cn.nukkit.entity.data.EntityFlag.GRAVITY
import cn.nukkit.entity.data.EntityFlag.SPRINTING
import cn.nukkit.entity.impl.BaseEntity
import cn.nukkit.entity.impl.EntityLiving
import cn.nukkit.event.entity.EntityDamageByEntityEvent
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.level.BlockPosition
import cn.nukkit.math.MathHelper
import cn.nukkit.math.Vector3f
import cn.nukkit.potion.Effect
import kotlin.math.abs

interface SmartEntity: EntityProperties, MoveLogic {
    val ai: EntityAI<*>
    val equipments get() = ai.equipments

    private inline val entity get() = this as Entity
    private inline val base get() = this as BaseEntity

    val velocityMultiplier: Float get() = entity.defaultVelocityMultiplier
    val jumpVelocity get() = 0.42F * jumpVelocityMultiplier
    val jumpVelocityMultiplier: Float get() {
        val multiplier = base.levelBlock.jumpVelocityMultiplier
        return if (multiplier == 1F) {
            base.level[base.velocityAffectingPos].jumpVelocityMultiplier
        } else {
            multiplier
        }
    }

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
        entity.setFlag(GRAVITY, true)
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
            updateAttacker() or
            tickMovement(tickDiff)

        entity.updateMovement()
        entity.updateData()
        return needUpdate
    }

    fun updateAttacker(): Boolean {
        val attacker = attacker ?: return false
        if (!attacker.isAlive || base.ticksLived - lastAttackedTime > 100) {
            this.attacker = null
            return false
        }
        return true
    }

    fun attack(source: EntityDamageEvent): Boolean {
        var entity = (source as? EntityDamageByEntityEvent)?.damager ?: return true
        if (entity is Projectile) {
            entity = entity.shooter ?: return true
        }
        attacker = entity
        lastAttackedTime = base.ticksLived
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
        //TODO Remove this debug code
        if ((ai.navigation.currentTarget?.distanceSquared(base) ?: 0.0) > 20.square()) {
            ai.navigation.stop()
            base.kill()
        }
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

    fun jump() { base.apply {
        var jumpVelocity = jumpVelocity
        if (hasEffect(Effect.JUMP)) {
            jumpVelocity += 0.1f * (getEffect(Effect.JUMP).amplifier + 1)
        }
        motion = Vector3f(motionX, jumpVelocity.toDouble(), motionZ)
        if (getFlag(SPRINTING)) {
            val g: Float = this.yaw.toFloat() * 0.017453292f
            motion = motion.add(-MathHelper.sin(g) * 0.2, 0.0, MathHelper.cos(g) * 0.2)
        }
    }}

    fun canTarget(entity: Entity) = entity is EntityLiving

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
        //TODO
        return true
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

    fun onAttacking(target: Entity) {
        attacking = target
        lastAttackTime = base.ticksLived
    }

    fun tryAttack(target: Entity): Boolean { base.apply {
        return true
        /*var bl: Boolean
        var i: Int
        var f = attribute(ATTACK_DAMAGE).value as Float
        var g = 5F //attribute(ATTACK_KNOCKBACK).value
        if (target is EntityLiving) {
            f += EnchantmentHelper.getAttackDamage(this.getMainHandStack(), (target as LivingEntity).getGroup())
            g += EnchantmentHelper.getKnockback(this) as Float
        }
        if (EnchantmentHelper.getFireAspect(this).also({ i = it }) > 0) {
            target.setOnFireFor(i * 4)
        }
        if (target.damage(DamageSource.mob(this), f).also({ bl = it })) {
            if (g > 0.0f && target is LivingEntity) {
                (target as LivingEntity).takeKnockback(
                    g * 0.5f,
                    MathHelper.sin(this.yaw * 0.017453292f),
                    -MathHelper.cos(this.yaw * 0.017453292f)
                )
                this.setVelocity(this.getVelocity().multiply(0.6, 1.0, 0.6))
            }
            if (target is PlayerEntity) {
                var playerEntity: PlayerEntity
                this.method_24521(
                    playerEntity,
                    this.getMainHandStack(),
                    if ((target as PlayerEntity?. also {
                            playerEntity = it
                        }).isUsingItem()) playerEntity.getActiveItem() else ItemStack.EMPTY
                )
            }
            this.dealDamage(this, target)
            onAttacking(target)
        }
        return bl*/
    }}
}
