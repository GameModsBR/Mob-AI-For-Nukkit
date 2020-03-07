package br.com.gamemods.mobai.entity.smart

import br.com.gamemods.mobai.entity.isSilent
import br.com.gamemods.mobai.entity.isTouchingWater
import br.com.gamemods.mobai.level.get
import br.com.gamemods.mobai.level.isAir
import br.com.gamemods.mobai.math.*
import cn.nukkit.block.*
import cn.nukkit.entity.impl.BaseEntity
import cn.nukkit.level.Sound
import cn.nukkit.math.Vector3f
import cn.nukkit.math.Vector3i
import cn.nukkit.network.protocol.LevelSoundEventPacket
import cn.nukkit.registry.BlockRegistry
import kotlin.math.sqrt

interface MoveLogic: Traveller, EntityProperties {
    private inline val base get() = this as BaseEntity
    private inline val smart get() = this as SmartEntity

    val landingPos: Vector3i get() { base.apply {
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

    fun moveToBoundingBoxCenter() { base.apply {
        val box = boundingBox
        setPosition(Vector3f((box.minX + box.maxX) / 2.0, box.minY, (box.minZ + box.maxZ) / 2.0))
    }}

    override fun move(cause: MoveCause, movement: Vector3f) { base.apply {  smart.apply {
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

    fun playSound(sound: Sound, volume: Float, pitch: Float) { base.apply {
        if (!isSilent) {
            level.addSound(position, sound, volume, pitch)
        }
    }}
}
