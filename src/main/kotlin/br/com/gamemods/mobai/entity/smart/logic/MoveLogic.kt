package br.com.gamemods.mobai.entity.smart.logic

import br.com.gamemods.mobai.entity.defaultVelocityMultiplier
import br.com.gamemods.mobai.entity.isSilent
import br.com.gamemods.mobai.entity.isTouchingWater
import br.com.gamemods.mobai.entity.smart.MoveCause
import br.com.gamemods.mobai.entity.velocityAffectingPos
import br.com.gamemods.mobai.level.get
import br.com.gamemods.mobai.level.isAir
import br.com.gamemods.mobai.level.isClimbable
import br.com.gamemods.mobai.level.jumpVelocityMultiplier
import br.com.gamemods.mobai.math.*
import cn.nukkit.block.*
import cn.nukkit.entity.data.EntityFlag
import cn.nukkit.level.BlockPosition
import cn.nukkit.level.Sound
import cn.nukkit.math.MathHelper
import cn.nukkit.math.Vector3f
import cn.nukkit.math.Vector3i
import cn.nukkit.network.protocol.LevelSoundEventPacket
import cn.nukkit.potion.Effect
import cn.nukkit.registry.BlockRegistry
import kotlin.math.sqrt

interface MoveLogic: TravellerLogic {
    val landingPos: Vector3i get() { base {
        val pos = Vector3i(x.intFloor(), (y - 0.20000000298023224).intFloor(), z.intFloor())
        val block = level.getBlock(pos)
        if (block is BlockFenceGate) {
            return pos.down()
        }
        if (block.isAir) {
            when (val down = block.down()) {
                is BlockFence, is BlockWall, is BlockFenceGate -> return down
            }
        }
        return pos
    }}

    val swimSound get() = Sound.RANDOM_SWIM

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
    val isClimbing: Boolean get() { base {
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

    fun moveToBoundingBoxCenter() { base {
        val box = boundingBox
        setPosition(Vector3f((box.minX + box.maxX) / 2.0, box.minY, (box.minZ + box.maxZ) / 2.0))
    }}

    override fun move(cause: MoveCause, movement: Vector3f) { base { smart {
        if (this.noClip) {
            boundingBox.offset(movement)
            this.moveToBoundingBoxCenter()
            return
        }

        var mov = movement
        if (movementMultiplier.lengthSquared() > 1.0E-7) {
            mov = movement * movementMultiplier
            movementMultiplier = ZERO_3F
            motion = ZERO_3F
        }

        //println("MV: $mov")

        val lastPos = position
        val landingPos = landingPos
        base.move(mov.x, mov.y, mov.z)

        if (vehicle == null) {
            distanceTraveled += lastPos.distance(base).toFloat()
            val landing = level[landingPos]
            if (distanceTraveled > nextStepSoundDistance && !landing.isAir) {
                nextStepSoundDistance = calculateNextStepSoundDistance()
                if (isTouchingWater) {
                    val passenger = passenger ?: base
                    val multiplier = if (passenger === this) 0.35F else 0.4F
                    val motion = passenger.motion
                    val vol = (sqrt(
                        motion.x.square() * 0.20000000298023224
                                + motion.y.square()
                                + motion.z.square() * 0.20000000298023224
                    ) * multiplier).coerceAtMost(1.0)
                    playSwimSound(vol.toFloat())
                } else {
                    playStepSound(landing)
                }
            }
        }

        val velocityMultiplier = velocityMultiplier.toDouble()
        motion = motion.multiply(velocityMultiplier, 1.0, velocityMultiplier)
    }}}

    fun calculateNextStepSoundDistance() = distanceTraveled.toInt() + 1F

    fun playStepSound(landing: Block) {
        if (landing is BlockLiquid || base.isSilent) {
            return
        }

        simpleStepSound?.let {
            playSound(it.sound, it.volume, it.pitch)
            return
        }

        val up = landing.up()
        val source = when (up.id) {
            BlockIds.SNOW, BlockIds.SNOW_LAYER -> up
            else -> landing
        }
        base.level.addLevelSoundEvent(base.position, LevelSoundEventPacket.SOUND_STEP, BlockRegistry.get().getRuntimeId(source))
    }

    fun playSwimSound(volume: Float) {
        this.playSound(swimSound, volume, 1.0f + (random.nextFloat() - random.nextFloat()) * 0.4f)
    }

    fun playSound(sound: Sound, volume: Float, pitch: Float) { base {
        if (!isSilent) {
            level.addSound(position, sound, volume, pitch)
        }
    }}

    fun swimUpLava() {
        base.motion = base.motion.add(0.0, 0.03999999910593033, 0.0)
    }

    fun swimUpWater() {
        base.motion = base.motion.add(0.0, 0.03999999910593033, 0.0)
    }

    fun jump() { base {
        var jumpVelocity = jumpVelocity
        if (hasEffect(Effect.JUMP)) {
            jumpVelocity += 0.1f * (getEffect(Effect.JUMP).amplifier + 1)
        }
        motion = Vector3f(motionX, jumpVelocity.toDouble(), motionZ)
        if (getFlag(EntityFlag.SPRINTING)) {
            val g: Float = this.yaw.toFloat() * 0.017453292f
            motion = motion.add(-MathHelper.sin(g) * 0.2, 0.0, MathHelper.cos(g) * 0.2)
        }
    }}

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
    fun updateMovementInclHead() { base {
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
    }}
}
