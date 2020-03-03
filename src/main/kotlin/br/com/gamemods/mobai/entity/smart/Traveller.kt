package br.com.gamemods.mobai.entity.smart

import br.com.gamemods.mobai.entity.*
import br.com.gamemods.mobai.level.FutureEffectIds
import br.com.gamemods.mobai.math.clamp
import cn.nukkit.block.BlockLadder
import cn.nukkit.block.BlockTrapdoor
import cn.nukkit.entity.data.EntityFlag
import cn.nukkit.entity.impl.BaseEntity
import cn.nukkit.item.enchantment.Enchantment
import cn.nukkit.math.MathHelper
import cn.nukkit.math.Vector3f
import cn.nukkit.potion.Effect

interface Traveller {
    private inline val base get() = this as BaseEntity
    private inline val smart get() = this as SmartEntity

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

    fun travel(movementInput: Vector3f) { base.apply { smart.apply {
        var d: Double
        var g: Float
        if (!isAiDisabled) {
            d = 0.08
            val bl: Boolean = motionY <= 0.0
            if (bl && hasEffect(Effect.SLOW_FALLING)) {
                d = 0.01
                fallDistance = 0.0f
            }
            val e: Double
            var f: Float
            val j: Double
            if (!isTouchingWater) {
                if (isInLava) {
                    e = getY()
                    updateVelocity(0.02f, movementInput)
                    move(MoveCause.SELF, motion)
                    motion = motion.multiply(0.5)
                    if (base.getFlag(EntityFlag.GRAVITY)) {
                        motion = motion.add(0.0, -d / 4.0, 0.0)
                    }
                    val vec3d4: Vector3f = motion
                    if (isCollidedHorizontally && !hasCollision(vec3d4.add(0.0, 0.6000000238418579 - getY() + e, 0.0))) {
                        motion = Vector3f(vec3d4.x, 0.30000001192092896, vec3d4.z)
                    }
                } //TODO else if (this.isFallFlying()) { ... -- Skipped elytra code
                else {
                    val blockPos = velocityAffectingPos.asVector3i()
                    val v: Float = level.getBlock(blockPos).frictionFactor.toFloat()
                    f = if (onGround) v * 0.91f else 0.91f
                    updateVelocity(getMovementSpeed(v), movementInput)
                    motion = this.applyClimbingSpeed(motion)
                    move(MoveCause.SELF, motion)
                    var vec3d7 = motion
                    if ((isCollidedHorizontally || isJumping) && isClimbing) {
                        vec3d7 = Vector3f(vec3d7.x, 0.2, vec3d7.z)
                    }
                    var x: Double = vec3d7.y
                    if (hasEffect(Effect.LEVITATION)) {
                        x += (0.05 * (getEffect(Effect.LEVITATION).amplifier + 1).toDouble() - vec3d7.y) * 0.2
                        resetFallDistance()
                    } else if (getFlag(EntityFlag.GRAVITY)) {
                        x -= d
                    }
                    motion = Vector3f(
                        vec3d7.x * f.toDouble(),
                        x * 0.9800000190734863,
                        vec3d7.z * f.toDouble()
                    )
                }
            } else {
                e = getY()
                f = if (getFlag(EntityFlag.SPRINTING)) 0.9f else this.baseMovementSpeedMultiplier
                g = 0.02f
                var h = getEquipmentLevel(Enchantment.ID_WATER_WALKER).toFloat()
                if (h > 3.0f) {
                    h = 3.0f
                }
                if (!onGround) {
                    h *= 0.5f
                }
                if (h > 0.0f) {
                    f += (0.54600006f - f) * h / 3.0f
                    g += (movementSpeed - g) * h / 3.0f
                }
                if (hasEffect(FutureEffectIds.DOLPHINS_GRACE)) {
                    f = 0.96f
                }
                this.updateVelocity(g, movementInput)
                move(MoveCause.SELF, motion)
                var vec3d = motion
                if (isCollidedHorizontally && isClimbing) {
                    vec3d = Vector3f(vec3d.x, 0.2, vec3d.z)
                }
                motion = vec3d.multiply(f.toDouble(), 0.800000011920929, f.toDouble())
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
                        vec3d2.y + 0.6000000238418579 - getY() + e,
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

    fun updateVelocity(speed: Float, movementInput: Vector3f) { base.apply {
        val vec3d: Vector3f = movementInputToVelocity(movementInput, speed, yaw.toFloat())
        motion = motion.add(vec3d)
    }}

    fun movementInputToVelocity(movementInput: Vector3f, speed: Float, yaw: Float): Vector3f {
        val d: Double = movementInput.lengthSquared()
        return if (d < 1.0E-7) {
            Vector3f()
        } else {
            val vec3d: Vector3f =
                (if (d > 1.0) movementInput.normalize() else movementInput).multiply(speed.toDouble())
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

    fun applyClimbingSpeed(motion: Vector3f): Vector3f { base.apply { smart.apply {
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
