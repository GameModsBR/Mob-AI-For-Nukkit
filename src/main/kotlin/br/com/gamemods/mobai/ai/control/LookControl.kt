package br.com.gamemods.mobai.ai.control

import br.com.gamemods.mobai.entity.smart.EntityAI
import br.com.gamemods.mobai.entity.smart.SmartEntity
import br.com.gamemods.mobai.math.MobAiMath
import cn.nukkit.entity.Entity
import cn.nukkit.entity.impl.BaseEntity
import cn.nukkit.math.Vector3f
import kotlin.math.atan2
import kotlin.math.sqrt

open class LookControl<E>(ai: EntityAI<E>) where E: SmartEntity, E: BaseEntity {
    protected val entity = ai.entity
    protected var yawSpeed = 0.0
    protected var pitchSpeed = 0.0
    protected var active = false
    protected var lookX = 0.0
    protected var lookY = 0.0
    protected var lookZ = 0.0

    fun lookAt(x: Double, y: Double, z: Double, yawSpeed: Double = entity.lookYawSpeed, pitchSpeed: Double = entity.lookPitchSpeed) {
        this.lookX = x
        this.lookY = y
        this.lookZ = z
        this.yawSpeed = yawSpeed
        this.pitchSpeed = pitchSpeed
        this.active = true
    }

    fun lookAt(pos: Vector3f, yawSpeed: Double = entity.lookYawSpeed, pitchSpeed: Double = entity.lookPitchSpeed)
            = lookAt(pos.x, pos.y, pos.z, yawSpeed, pitchSpeed)

    fun lookAt(entity: Entity, yawSpeed: Double = this.entity.lookYawSpeed, pitchSpeed: Double = this.entity.lookPitchSpeed)
            = lookAt(entity.x, entity.y + entity.eyeHeight, entity.z, yawSpeed, pitchSpeed)

    fun shouldNotPitch() = true

    fun tick(): Boolean {
        var pitch = entity.pitch
        var yaw = entity.yaw
        var headYaw = this.entity.headYaw

        val iniPitch = pitch
        val iniYaw = yaw
        val iniHeadYaw = headYaw

        if (shouldNotPitch()) {
            pitch = 0.0
        }

        if (active) {
            active = false
            yaw = changeAngle(yaw, targetYaw(), yawSpeed)
            //headYaw = yaw
            pitch = changeAngle(pitch, targetPitch(), pitchSpeed)
        } else {
            headYaw = changeAngle(headYaw, yaw, entity.lookYawSpeed)
        }

        if (entity.ai.navigation.isActive) {
            headYaw = changeAngle(headYaw, yaw, entity.lookMovingSpeed)
        }

        if (iniPitch != pitch || iniYaw != yaw || iniHeadYaw != headYaw) {
            entity.setRotation(yaw, pitch, headYaw)
        }

        return true
    }

    protected open fun targetPitch(): Double {
        val x = lookX - entity.x
        val y = lookY - entity.y - entity.eyeHeight
        val z = lookZ - entity.z
        val a = sqrt(x * x + z * z)
        return -(atan2(y, a) * MobAiMath.RAD2DEG_F)
    }

    protected open fun targetYaw(): Double {
        val x = lookX - entity.x
        val z = lookZ - entity.z
        return (atan2(z, x) * MobAiMath.RAD2DEG_F) - 90.0
    }

    open fun changeAngle(from: Double, to: Double, max: Double): Double {
        val a = MobAiMath.subtractAngles(from, to)
        val b = MobAiMath.clamp(a, -max, max)
        return from + b
    }

    protected open fun changeAngleSubtracting(from: Double, to: Double, max: Double): Double {
        val a = MobAiMath.subtractAngles(from, to)
        val b = MobAiMath.clamp(a, -max, max)
        return from - b
    }
}
