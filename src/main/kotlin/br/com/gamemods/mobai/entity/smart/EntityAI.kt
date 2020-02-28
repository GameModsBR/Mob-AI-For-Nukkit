package br.com.gamemods.mobai.entity.smart

import br.com.gamemods.mobai.ai.control.LookControl
import br.com.gamemods.mobai.ai.goal.GoalSelector
import br.com.gamemods.mobai.ai.pathing.EntityNavigation
import br.com.gamemods.mobai.ai.pathing.WalkingNavigation
import br.com.gamemods.mobai.math.MobAiMath
import cn.nukkit.entity.Attribute
import cn.nukkit.entity.Entity
import cn.nukkit.entity.impl.BaseEntity
import cn.nukkit.math.Vector3f
import java.util.*
import java.util.concurrent.ThreadLocalRandom

class EntityAI(
    val entity: BaseEntity,
    val lookPitchSpeed: Double = 40.0,
    val lookMovingSpeed: Double = 75.0,
    val lookYawSpeed: Double = 10.0,
    lookControlFactory: (EntityAI) -> LookControl = ::LookControl,
    navigationFactory: (EntityAI) -> EntityNavigation = ::WalkingNavigation
) {
    var headYaw = entity.yaw
    var lastHeadYaw = entity.lastYaw

    val goalSelector = GoalSelector()
    val targetSelector = GoalSelector()
    val lookControl = lookControlFactory(this)
    val navigation = navigationFactory(this)

    val attributes = mutableMapOf<Int, Attribute>()

    var target: Entity? = null

    val random: Random get() = ThreadLocalRandom.current()

    fun setRotation(yaw: Double, pitch: Double, headYaw: Double) {
        this.headYaw = headYaw
        entity.setRotation(yaw, pitch)
    }

    fun setPositionAndRotation(pos: Vector3f, yaw: Double, pitch: Double, headYaw: Double): Boolean {
        if (entity.setPositionAndRotation(pos, yaw, pitch)) {
            this.headYaw = headYaw
            return true
        }
        return false
    }

    fun onSmartEntityUpdate(currentTick: Int): Boolean {
        val entity = entity
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

        val tickDiff = currentTick - entity.lastUpdate

        if (tickDiff <= 0) {
            return false
        }

        entity.lastUpdate = currentTick

        val needsUpdate = targetSelector.tick() or
                goalSelector.tick() or
                navigation.tick() or
                entity.entityBaseTick(tickDiff) or
                //moveControl.tick() or
                lookControl.tick()// or
                //jumpControl.tick()

        entity.updateMovement()
        entity.updateData()

        return needsUpdate
    }

    fun updateMovementInclHead() {
        entity.apply {
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
