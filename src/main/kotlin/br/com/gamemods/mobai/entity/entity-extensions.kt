package br.com.gamemods.mobai.entity

import br.com.gamemods.mobai.ai.pathing.PathNodeType
import br.com.gamemods.mobai.entity.smart.EntityProperties
import br.com.gamemods.mobai.entity.smart.EntityPropertyStorage
import br.com.gamemods.mobai.entity.smart.SmartEntity
import br.com.gamemods.mobai.level.get
import br.com.gamemods.mobai.level.getBlockIdAt
import br.com.gamemods.mobai.level.hasCollision
import br.com.gamemods.mobai.level.velocityMultiplier
import br.com.gamemods.mobai.math.offsetCopy
import cn.nukkit.block.BlockIds.*
import cn.nukkit.entity.Attribute
import cn.nukkit.entity.Entity
import cn.nukkit.entity.EntityDamageable
import cn.nukkit.entity.data.EntityFlag
import cn.nukkit.entity.impl.BaseEntity
import cn.nukkit.entity.impl.EntityLiving
import cn.nukkit.entity.impl.Human
import cn.nukkit.item.Item
import cn.nukkit.item.enchantment.Enchantment
import cn.nukkit.level.BlockPosition
import cn.nukkit.level.Position
import cn.nukkit.math.Vector3f

private val defaultProperties = EntityPropertyStorage(0.0)

val Entity.eyePosition get() = Position(x, y + eyeHeight, z, level)
val Entity.isTouchingWater get() = (this as? BaseEntity)?.isInsideOfWater ?: false
val Entity.isInLava get() = level.getBlockIdAt(position.asVector3i()).let { it == LAVA || it == FLOWING_LAVA }
val Entity.properties get() = this as? EntityProperties ?: defaultProperties
val Entity.propertiesOrNul get() = this as? EntityProperties
//TODO Not checking if is inside of water when the entity does not extends BaseEntity
val Entity.isInsideOfWaterOrBubbles get() = (this as? BaseEntity)?.isInsideOfWaterOrBubbles ?: isInsideOfBubbles
val Entity.isInsideOfBubbles get() = level.getBlockIdAt(position.asVector3i()) == BUBBLE_COLUMN
val BaseEntity.isInsideOfWaterOrBubbles get() = isInsideOfWater || level.getBlockIdAt(asVector3i()) == BUBBLE_COLUMN
val Entity.velocityAffectingPos get() = Vector3f(x, boundingBox.minY - 0.5000001, z)

val Entity.velocityMultiplier get() = (this as? SmartEntity)?.velocityMultiplier ?: defaultVelocityMultiplier
val Entity.defaultVelocityMultiplier: Float get() {
    val block = level[position]

    val multiplier = block.velocityMultiplier
    return when (block.id) {
        WATER, FLOWING_WATER, BUBBLE_COLUMN -> return multiplier
        else -> if (multiplier == 1F) level[velocityAffectingPos].velocityMultiplier else multiplier
    }
}

var Entity.movementSpeed: Float
    get() = (this as? EntityLiving)?.movementSpeed ?: 0.1F
    set(value) {
        (this as? EntityLiving)?.movementSpeed = value
    }

var Entity.forwardMovementSpeed: Float
    get() = (this as? EntityProperties)?.forwardSpeed ?: 0F
    set(value) {
        (this as? EntityProperties)?.forwardSpeed = value
        (this as? EntityLiving)?.movementSpeed = value
    }

var Entity.flyingSpeed: Float
    get() = properties.flyingSpeed
    set(value) {
        propertiesOrNul?.flyingSpeed = value
    }

fun Entity.hasCollision(pos: Vector3f): Boolean {
    val box = boundingBox.offsetCopy(pos)
    return level.hasCollision(this as BaseEntity, box, entities = true, fluids = true)
}

fun Entity.canTarget(entity: Entity) = (this as? SmartEntity)?.canTarget(entity) ?: entity is EntityDamageable
fun Entity.isTeammate(entity: Entity) = (this as? SmartEntity)?.isTeammate(entity) ?: false
fun Entity.canSee(entity: Entity) = (this as? SmartEntity)?.canSee(entity) ?: false
fun Entity.pathFindingPenalty(nodeType: PathNodeType)
        = propertiesOrNul?.pathFindingPenalties?.get(nodeType) ?: nodeType.defaultPenalty

var Entity.isImmobile: Boolean
    get() = getFlag(EntityFlag.IMMOBILE)
    set(value) = setFlag(EntityFlag.IMMOBILE, value)

val Entity.isDeadOrImmobile get() = health <= 0 || isImmobile

fun Entity.getEquipmentLevel(enchantmentId: Int): Int {
    return (this as? SmartEntity)?.getEquipmentLevel(enchantmentId)
        ?: (this as? Human)?.getEquipmentLevel(enchantmentId)
        ?: 0
}

fun SmartEntity.getEquipmentLevel(enchantmentId: Int): Int {
    return equipments.subList(0, 4).asSequence().getEquipmentLevel(enchantmentId)
}

fun Human.getEquipmentLevel(enchantmentId: Int): Int {
    return inventory.armorContents.asSequence().getEquipmentLevel(enchantmentId)
}

private fun Sequence<Item>.getEquipmentLevel(enchantmentId: Int): Int {
    return mapNotNull { it.getEnchantment(enchantmentId) }.map(Enchantment::getLevel).max() ?: 0
}

tailrec fun Entity.pathFindingFavor(pos: BlockPosition): Float {
    return if (pos.layer != 0) {
        pathFindingFavor(pos.layer(0))
    } else {
        (this as? SmartEntity)?.pathFindingFavor(pos) ?: 0F
    }
}

inline fun Entity.attribute(id: Int, fallback: (Entity, Int) -> Attribute = { _, i -> Attribute.getAttribute(i) })
        = propertiesOrNul?.attributes?.get(id) ?: fallback(this, id)

tailrec fun Entity.isRiding(entity: Entity): Boolean {
    val vehicle = vehicle ?: return false
    if (vehicle == entity) {
        return true
    }
    return vehicle.isRiding(entity)
}

fun Entity.getMovementSpeed(slipperiness: Float): Float {
    return if (isOnGround) {
        movementSpeed * ((0.6F * 0.6F * 0.6F) / (slipperiness * slipperiness * slipperiness))
    } else {
        flyingSpeed
    }
}
