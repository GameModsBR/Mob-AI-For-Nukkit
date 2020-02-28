package br.com.gamemods.mobai.ai.control

import br.com.gamemods.mobai.entity.smart.EntityAI
import br.com.gamemods.mobai.math.MobAiMath
import cn.nukkit.entity.Entity
import cn.nukkit.math.Vector3f
import kotlin.math.atan2
import kotlin.math.sqrt

open class LookControl(val ai: EntityAI) {
    protected var yawSpeed = 0.0
    protected var pitchSpeed = 0.0
    protected var active = false
    protected var lookX = 0.0
    protected var lookY = 0.0
    protected var lookZ = 0.0

    fun lookAt(x: Double, y: Double, z: Double, yawSpeed: Double = ai.lookYawSpeed, pitchSpeed: Double = ai.lookPitchSpeed) {
        this.lookX = x
        this.lookY = y
        this.lookZ = z
        this.yawSpeed = yawSpeed
        this.pitchSpeed = pitchSpeed
        this.active = true
    }

    fun lookAt(pos: Vector3f, yawSpeed: Double = ai.lookYawSpeed, pitchSpeed: Double = ai.lookPitchSpeed)
            = lookAt(pos.x, pos.y, pos.z, yawSpeed, pitchSpeed)

    fun lookAt(entity: Entity, yawSpeed: Double = ai.lookYawSpeed, pitchSpeed: Double = ai.lookPitchSpeed)
            = lookAt(entity.x, entity.y + entity.eyeHeight, entity.z, yawSpeed, pitchSpeed)

    fun shouldNotPitch() = true

    fun tick(): Boolean {
        val entity = ai.entity
        var pitch = entity.pitch
        var yaw = entity.yaw
        var headYaw = ai.headYaw

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
            headYaw = changeAngle(headYaw, yaw, 10.0)
        }

        if (!ai.navigation.isIdle) {
            headYaw = changeAngleSubtracting(headYaw, yaw, ai.lookMovingSpeed)
        }

        if (iniPitch != pitch || iniYaw != yaw || iniHeadYaw != headYaw) {
            ai.setRotation(yaw, pitch, headYaw)
        }

        return true
    }

    protected fun targetPitch(): Double {
        val entity = ai.entity
        val x = lookX - entity.x
        val y = lookY - entity.y - entity.eyeHeight
        val z = lookZ - entity.z
        val a = sqrt(x * x + z * z)
        return -(atan2(y, a) * 57.2957763671875)
    }

    protected fun targetYaw(): Double {
        val entity = ai.entity
        val x = lookX - entity.x
        val z = lookZ - entity.z
        return (atan2(z, x) * 57.2957763671875) - 90.0
    }

    protected fun changeAngle(from: Double, to: Double, max: Double): Double {
        val a = MobAiMath.subtractAngles(from, to)
        val b = MobAiMath.clamp(a, -max, max)
        return from + b
    }

    protected fun changeAngleSubtracting(from: Double, to: Double, max: Double): Double {
        val a = MobAiMath.subtractAngles(from, to)
        val b = MobAiMath.clamp(a, -max, max)
        return from - b
    }
}
