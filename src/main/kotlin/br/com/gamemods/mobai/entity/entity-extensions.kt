package br.com.gamemods.mobai.entity

import br.com.gamemods.mobai.delegators.NoOp
import br.com.gamemods.mobai.delegators.priority
import br.com.gamemods.mobai.entity.smart.EntityProperties
import br.com.gamemods.mobai.entity.smart.EntityPropertyStorage
import br.com.gamemods.mobai.entity.smart.SmartEntity
import br.com.gamemods.mobai.level.get
import br.com.gamemods.mobai.level.getBlockIdAt
import br.com.gamemods.mobai.level.hasCollision
import br.com.gamemods.mobai.level.velocityMultiplier
import br.com.gamemods.mobai.math.offsetCopy
import cn.nukkit.block.BlockIds.*
import cn.nukkit.block.BlockWater
import cn.nukkit.entity.Attribute
import cn.nukkit.entity.Entity
import cn.nukkit.entity.data.EntityData
import cn.nukkit.entity.data.EntityFlag
import cn.nukkit.entity.impl.BaseEntity
import cn.nukkit.entity.impl.EntityLiving
import cn.nukkit.entity.impl.Human
import cn.nukkit.item.Item
import cn.nukkit.item.enchantment.Enchantment
import cn.nukkit.level.BlockPosition
import cn.nukkit.level.Position
import cn.nukkit.math.Vector3f
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.player.Player
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

private val defaultProperties = EntityPropertyStorage(CompoundTag())

val Entity.eyePosition get() = Position(x, y + eyeHeight, z, level)
val Entity.isTouchingWater get() = (this as? BaseEntity)?.isInsideOfWater ?: false
val Entity.isInLava get() = level.getBlockIdAt(position.asVector3i()).let { it == LAVA || it == FLOWING_LAVA }
val Entity.propertiesForReading get() = this as? EntityProperties ?: defaultProperties
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

var Entity.forwardMovementSpeed: Float
    get() = (this as? EntityProperties)?.forwardSpeed ?: 0F
    set(value) {
        (this as? EntityProperties)?.forwardSpeed = value
        (this as? EntityLiving)?.movementSpeed = value
    }

var Entity.movementSpeed by priority(EntityLiving::getMovementSpeed, EntityLiving::setMovementSpeed, NoOp(0.0F))
var Entity.maxAir by ShortData(EntityData.MAX_AIR)
var Entity.remainingAir by ShortData(EntityData.AIR)
var Entity.isNameTagAlwaysVisible by priority(BaseEntity::isNameTagAlwaysVisible, BaseEntity::setNameTagAlwaysVisible, BooleanData(EntityData.ALWAYS_SHOW_NAMETAG))
var Entity.isSilent by Flag(EntityFlag.SILENT)
var Entity.isImmobile by Flag(EntityFlag.IMMOBILE)
var Entity.isUsingItem by priority(Player::isUsingItem, Player::setUsingItem, Flag(EntityFlag.ACTION))
var Entity.isSneaking by priority(Human::isSneaking, Human::setSneaking, Flag(EntityFlag.SNEAKING))
var Entity.isSprinting by priority(Human::isSprinting, Human::setSprinting, Flag(EntityFlag.SPRINTING))

fun Entity.hasCollision(pos: Vector3f): Boolean {
    val box = boundingBox.offsetCopy(pos)
    return level.hasCollision(this as BaseEntity, box, entities = true, fluids = true)
}

fun Entity.canTarget(entity: Entity) = (this as? SmartEntity)?.canTarget(entity) ?: (entity is EntityLiving)
fun Entity.isTeammate(entity: Entity) = (this as? SmartEntity)?.isTeammate(entity) ?: false
fun Entity.canSee(entity: Entity) = (this as? SmartEntity)?.canSee(entity) ?: false
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
        = (this as? EntityProperties)?.attributes?.get(id) ?: fallback(this, id)

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
        propertiesForReading.flyingSpeed
    }
}

fun Entity.addFlags(first: EntityFlag, vararg others: EntityFlag) {
    sequenceOf(first, *others).forEach {
        setFlag(it, true)
    }
}

val Entity.waterHeight: Double get() {
    val eyePosition = eyePosition
    val water = level[eyePosition] as? BlockWater ?: level[eyePosition, 1] as? BlockWater ?: return 0.0
    return if(water.damage == 0) 1.0 else water.fluidHeightPercent.toDouble()
}

val Entity.mainHandItem: Item get() = when (this) {
    is Player -> inventory.itemInHand
    is SmartEntity -> equipments.mainHand
    else -> Item.get(AIR)
}

val Entity.offHandItem: Item get() = when (this) {
    is SmartEntity -> equipments.offHand
    else -> Item.get(AIR)
}


val Entity.handItems get() = arrayOf(mainHandItem, offHandItem)

val Entity.lootingLevel get() = mainHandItem.getEnchantment(Enchantment.ID_LOOTING)?.level ?: 0

var Attribute.baseValue: Float
    get() = value
    set(value) {
        this.defaultValue = value
        this.value = value
    }

// The way Nukkit designed entities makes this get called before this object is fully setup,
// causing NPE on instantiation
inline fun SmartEntity.ifNotOnInit(action: () -> Unit) {
    try {
        definitions.hashCode()
    } catch (_: NullPointerException) {
        return
    }

    action()
}

class Property<V>(private val property: KMutableProperty<V>): ReadWriteProperty<Entity, V> {
    override fun getValue(thisRef: Entity, property: KProperty<*>): V {
        return this.property.getter.call(thisRef as? EntityProperties ?: defaultProperties)
    }

    override fun setValue(thisRef: Entity, property: KProperty<*>, value: V) {
        this.property.setter.call(thisRef as? EntityProperties ?: return, value)
    }
}
