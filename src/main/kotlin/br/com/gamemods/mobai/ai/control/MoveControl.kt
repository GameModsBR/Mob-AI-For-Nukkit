package br.com.gamemods.mobai.ai.control

import br.com.gamemods.mobai.ai.control.MoveControl.State.*
import br.com.gamemods.mobai.ai.pathing.PathNodeType
import br.com.gamemods.mobai.entity.attribute
import br.com.gamemods.mobai.entity.forwardMovementSpeed
import br.com.gamemods.mobai.entity.movementSpeed
import br.com.gamemods.mobai.entity.smart.EntityAI
import br.com.gamemods.mobai.entity.smart.SmartEntity
import br.com.gamemods.mobai.math.MobAiMath
import br.com.gamemods.mobai.math.isEmpty
import br.com.gamemods.mobai.math.square
import cn.nukkit.block.BlockDoor
import cn.nukkit.block.BlockFence
import cn.nukkit.entity.Attribute.MOVEMENT_SPEED
import cn.nukkit.entity.impl.BaseEntity
import cn.nukkit.math.AxisAlignedBB
import cn.nukkit.math.MathHelper
import cn.nukkit.math.Vector3f
import kotlin.math.atan2
import kotlin.math.max

open class MoveControl<E>(ai: EntityAI<E>) where E: SmartEntity, E: BaseEntity {
    protected val entity = ai.entity
    protected lateinit var target: Vector3f
    var speed = 0.0; protected set
    protected var forwardMovement = 0F
    protected var sidewaysMovement = 0F
    protected var state = WAIT

    val isMoving get() = state == MOVE_TO

    fun moveTo(target: Vector3f, speed: Double) {
        println("TG: $target")
        this.target = target
        this.speed = speed
        if (state != JUMPING) {
            state = MOVE_TO
        }
    }

    fun strafeTo(forward: Float, sideways: Float) {
        state = STRAFE
        forwardMovement = forward
        sidewaysMovement = sideways
        speed = 0.25
    }

    fun tick(): Boolean {
        return when (state) {
            STRAFE -> processStrafe()
            MOVE_TO -> processMove()
            JUMPING -> processJump()
            WAIT -> processWait()
        }
    }

    protected fun processStrafe(): Boolean {
        val entity = entity
        val movementSpeedAttribute = entity.attribute(MOVEMENT_SPEED).value
        var movementSpeed = (speed * movementSpeedAttribute).toFloat()
        var forwardMovement = forwardMovement
        var sidewaysMovement = sidewaysMovement
        var normalized = MathHelper.sqrt(forwardMovement.square() + sidewaysMovement.square())
        if (normalized < 1.0f) {
            normalized = 1.0f
        }
        normalized = movementSpeed / normalized
        forwardMovement *= normalized
        sidewaysMovement *= normalized
        val sin = MathHelper.sin(entity.yaw * 0.017453292f)
        val cos = MathHelper.cos(entity.yaw * 0.017453292f)
        val x = forwardMovement * cos - sidewaysMovement * sin
        val z = sidewaysMovement * cos + forwardMovement * sin
        val entityNavigation = entity.ai.navigation
        val pathNodeMaker = entityNavigation.nodeMaker
        if (pathNodeMaker.nodeType(
                entity.level,
                entity.add(x.toDouble(), 0.0, z.toDouble()).asVector3i(),
                entity
            ) != PathNodeType.WALKABLE
        ) {
            this.forwardMovement = 1.0f
            this.sidewaysMovement = 0.0f
            movementSpeed = movementSpeedAttribute
        }
        entity.movementSpeed = movementSpeed
        entity.forwardSpeed = this.forwardMovement
        entity.sidewaysSpeed = this.sidewaysMovement
        state = WAIT
        return true
    }

    protected fun processMove(): Boolean {
        state = WAIT
        val x = target.x - entity.x
        val y = target.y - entity.y
        val z = target.z - entity.z
        val p = x.square() + z.square() + y.square()
        if (p < 2.500000277905201E-7) {
            entity.forwardSpeed = 0.0f
            return true
        }
        val angle = (atan2(z, x) * MobAiMath.RAD2DEG_F).toFloat() - 90.0f
        entity.yaw = changeAngle(entity.yaw.toFloat(), angle, 90.0f).toDouble()
        entity.forwardMovementSpeed = (speed * entity.attribute(MOVEMENT_SPEED).value).toFloat()
        val blockPos = entity.asVector3i()
        val block = entity.level.getBlock(blockPos)
        val bb: AxisAlignedBB? = block.boundingBox
        if (z > entity.stepHeight.toDouble() && x.square() + y.square() < max(1.0f, entity.width).toDouble()
            || bb?.isEmpty == false && entity.y < bb.maxY && block !is BlockDoor && block !is BlockFence
        ) {
            entity.ai.jumpControl.setActive()
            state = JUMPING
        }
        return true
    }

    protected fun processJump(): Boolean {
        entity.forwardMovementSpeed = (speed * entity.attribute(MOVEMENT_SPEED).value).toFloat()
        if (entity.isOnGround) {
            state = WAIT
        }
        return true
    }

    protected fun processWait(): Boolean {
        entity.forwardSpeed = 0F
        return true
    }

    protected fun changeAngle(from: Float, to: Float, max: Float): Float {
        var degrees = MobAiMath.wrapDegrees(to - from)
        if (degrees > max) {
            degrees = max
        } else if (degrees < -max) {
            degrees = -max
        }

        var newAngle = from + degrees
        if (newAngle < 0F) {
            newAngle += 360F
        } else if (newAngle > 360F) {
            newAngle -= 360F
        }

        return newAngle
    }

    enum class State {
        WAIT,
        MOVE_TO,
        STRAFE,
        JUMPING
    }
}
