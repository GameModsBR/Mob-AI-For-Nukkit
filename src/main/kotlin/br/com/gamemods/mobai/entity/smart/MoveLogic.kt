package br.com.gamemods.mobai.entity.smart

import br.com.gamemods.mobai.math.ZERO_3F
import br.com.gamemods.mobai.math.offset
import br.com.gamemods.mobai.math.times
import cn.nukkit.entity.impl.BaseEntity
import cn.nukkit.math.Vector3f

interface MoveLogic: Traveller, EntityProperties {
    private inline val base get() = this as BaseEntity
    private inline val smart get() = this as SmartEntity

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

        base.move(mov.x, mov.y, mov.z)

        //val velocityMultiplier = velocityMultiplier.toDouble()
        //motion = motion.multiply(velocityMultiplier, 1.0, velocityMultiplier)
    }}}
}
