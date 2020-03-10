package br.com.gamemods.mobai.entity.smart.logic

import br.com.gamemods.mobai.entity.*
import br.com.gamemods.mobai.entity.smart.MoveCause
import br.com.gamemods.mobai.level.FutureEffectIds
import br.com.gamemods.mobai.math.ZERO_3F
import br.com.gamemods.mobai.math.clamp
import cn.nukkit.block.BlockLadder
import cn.nukkit.block.BlockTrapdoor
import cn.nukkit.entity.data.EntityFlag
import cn.nukkit.item.enchantment.Enchantment
import cn.nukkit.math.MathHelper
import cn.nukkit.math.Vector3f
import cn.nukkit.potion.Effect

interface TravellerLogic: SplitLogic {
    fun canEnterTrapdoor(block: BlockTrapdoor): Boolean {
        if (!block.isOpen) {
            return false
        }

        val under = block.down()
        if (under !is BlockLadder) {
            return false
        }

        return under.blockFace == block.blockFace
    }

    fun travel(movementInput: Vector3f) { base { smart {
        //println("TR: $movementInput")
        var d: Double
        var g: Float
        if (!isAiDisabled) {
            d = 0.08
            val bl: Boolean = motionY <= 0.0
            if (bl && hasEffect(Effect.SLOW_FALLING)) {
                d = 0.01
                fallDistance = 0.0f
            }
            val y: Double
            var friction: Float
            val j: Double
            if (!isTouchingWater) {
                if (isInLava) {
                    y = getY()
                    updateVelocity(0.02f, movementInput)
                    move(MoveCause.SELF, motion)
                    motion = motion.multiply(0.5)
                    if (base.getFlag(EntityFlag.GRAVITY)) {
                        motion = motion.add(0.0, -d / 4.0, 0.0)
                    }
                    val vec3d4: Vector3f = motion
                    if (isCollidedHorizontally && !hasCollision(vec3d4.add(0.0, 0.6000000238418579 - getY() + y, 0.0))) {
                        motion = Vector3f(vec3d4.x, 0.30000001192092896, vec3d4.z)
                    }
                } //TODO else if (this.isFallFlying()) { ... -- Skipped elytra code
                else {
                    val blockPos = velocityAffectingPos.asVector3i()
                    val blockFriction: Float = level.getBlock(blockPos).frictionFactor.toFloat()
                    friction = if (onGround) blockFriction * 0.91f else 0.91f
                    updateVelocity(getMovementSpeed(blockFriction), movementInput)
                    motion = applyClimbingSpeed(motion)
                    move(MoveCause.SELF, motion)
                    var vec3d7 = motion
                    if ((isCollidedHorizontally || isJumping) && isClimbing) {
                        vec3d7 = Vector3f(vec3d7.x, 0.2, vec3d7.z)
                    }
                    var motY: Double = vec3d7.y
                    if (hasEffect(Effect.LEVITATION)) {
                        motY += (0.05 * (getEffect(Effect.LEVITATION).amplifier + 1).toDouble() - vec3d7.y) * 0.2
                        resetFallDistance()
                    } else if (getFlag(EntityFlag.GRAVITY)) {
                        motY -= d
                    }
                    motion = Vector3f(
                        vec3d7.x * friction.toDouble(),
                        motY * 0.9800000190734863,
                        vec3d7.z * friction.toDouble()
                    )
                }
            } else {
                y = getY()
                friction = if (getFlag(EntityFlag.SPRINTING)) 0.9f else this.baseMovementSpeedMultiplier
                g = 0.02f
                var h = getEquipmentLevel(Enchantment.ID_WATER_WALKER).toFloat()
                if (h > 3.0f) {
                    h = 3.0f
                }
                if (!onGround) {
                    h *= 0.5f
                }
                if (h > 0.0f) {
                    friction += (0.54600006f - friction) * h / 3.0f
                    g += (movementSpeed - g) * h / 3.0f
                }
                if (hasEffect(FutureEffectIds.DOLPHINS_GRACE)) {
                    friction = 0.96f
                }
                this.updateVelocity(g, movementInput)
                move(MoveCause.SELF, motion)
                var vec3d = motion
                if (isCollidedHorizontally && isClimbing) {
                    vec3d = Vector3f(vec3d.x, 0.2, vec3d.z)
                }
                motion = vec3d.multiply(friction.toDouble(), 0.800000011920929, friction.toDouble())
                var vec3d2: Vector3f
                if (getFlag(EntityFlag.GRAVITY) && !getFlag(EntityFlag.SPRINTING)) {
                    vec3d2 = motion
                    j = if (bl && kotlin.math.abs(vec3d2.y - 0.005) >= 0.003 && kotlin.math.abs(vec3d2.y - d / 16.0) < 0.003) {
                        -0.003
                    } else {
                        vec3d2.y - d / 16.0
                    }
                    motion = Vector3f(vec3d2.x, j, vec3d2.z)
                }
                vec3d2 = motion
                if (isCollidedHorizontally && !hasCollision(Vector3f(
                        vec3d2.x,
                        vec3d2.y + 0.6000000238418579 - getY() + y,
                        vec3d2.z
                    ))
                ) {
                    motion = Vector3f(vec3d2.x, 0.30000001192092896, vec3d2.z)
                }
            }
        }

        /*this.lastLimbDistance = this.limbDistance
        d = getX() - this.prevX
        val z: Double = getZ() - this.prevZ
        val aa = if (this is Flutterer) getY() - this.prevY else 0.0
        g = MathHelper.sqrt(d * d + aa * aa + z * z) * 4.0f
        if (g > 1.0f) {
            g = 1.0f
        }

        this.limbDistance += (g - this.limbDistance) * 0.4f
        this.limbAngle += this.limbDistance*/
    }}}

    fun updateVelocity(speed: Float, movementInput: Vector3f) { base {
        val vec3d: Vector3f = movementInputToVelocity(movementInput, speed, yaw.toFloat())
        motion = motion.add(vec3d)
    }}

    fun movementInputToVelocity(movementInput: Vector3f, speed: Float, yaw: Float): Vector3f {
        val length: Double = movementInput.lengthSquared()
        return if (length < 1.0E-7) {
            ZERO_3F
        } else {
            val vec3d: Vector3f =
                (if (length > 1.0) movementInput.normalize() else movementInput).multiply(speed.toDouble())
            val f: Float = MathHelper.sin(yaw * 0.017453292f)
            val g: Float = MathHelper.cos(yaw * 0.017453292f)
            Vector3f(
                vec3d.x * g.toDouble() - vec3d.z * f.toDouble(),
                vec3d.y,
                vec3d.z * g.toDouble() + vec3d.x * f.toDouble()
            )
        }
    }

    fun move(cause: MoveCause, movement: Vector3f)

    fun applyClimbingSpeed(motion: Vector3f): Vector3f { base { smart {
        if (!isClimbing) {
            return motion
        }

        resetFallDistance()
        val x = motion.x.clamp(-0.15000000596046448, 0.15000000596046448)
        val z = motion.z.clamp(-0.15000000596046448, 0.15000000596046448)
        val y = motion.y.coerceAtLeast(-0.15000000596046448)
        return Vector3f(x, y, z)
    }}}

}
