package br.com.gamemods.mobai.entity

import br.com.gamemods.mobai.delegators.priority.NoOp
import br.com.gamemods.mobai.delegators.priority.priority
import cn.nukkit.entity.Entity
import cn.nukkit.entity.data.EntityData
import cn.nukkit.entity.data.EntityFlag
import cn.nukkit.entity.impl.BaseEntity
import cn.nukkit.entity.impl.EntityLiving
import cn.nukkit.entity.impl.Human
import cn.nukkit.player.Player
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

var Entity.movementSpeed by priority(EntityLiving::getMovementSpeed, EntityLiving::setMovementSpeed, NoOp(0.0F))
var Entity.maxAir by ShortData(EntityData.MAX_AIR)
var Entity.remainingAir by ShortData(EntityData.AIR)
var Entity.isNameTagAlwaysVisible by priority(BaseEntity::isNameTagAlwaysVisible, BaseEntity::setNameTagAlwaysVisible, BooleanData(EntityData.ALWAYS_SHOW_NAMETAG))
var Entity.isSilent by Flag(EntityFlag.SILENT)
var Entity.isImmobile by Flag(EntityFlag.IMMOBILE)
var Entity.isUsingItem by priority(Player::isUsingItem, Player::setUsingItem, Flag(EntityFlag.ACTION))
var Entity.isSneaking by priority(Human::isSneaking, Human::setSneaking, Flag(EntityFlag.SNEAKING))
var Entity.isSprinting by priority(Human::isSprinting, Human::setSprinting, Flag(EntityFlag.SPRINTING))
var Entity.isBaby by Flag(EntityFlag.BABY)
val Entity.isAdult by InverseFlag(EntityFlag.BABY)

class IntData(private val data: EntityData): ReadWriteProperty<Entity, Int> {
    override fun getValue(thisRef: Entity, property: KProperty<*>): Int {
        return thisRef.getIntData(data)
    }

    override fun setValue(thisRef: Entity, property: KProperty<*>, value: Int) {
        thisRef.setIntData(data, value)
    }
}

class ShortData(private val data: EntityData): ReadWriteProperty<Entity, Int> {
    override fun getValue(thisRef: Entity, property: KProperty<*>): Int {
        return thisRef.getShortData(data).toInt()
    }

    override fun setValue(thisRef: Entity, property: KProperty<*>, value: Int) {
        thisRef.setShortData(data, value)
    }
}

class BooleanData(private val data: EntityData): ReadWriteProperty<Entity, Boolean> {
    override fun getValue(thisRef: Entity, property: KProperty<*>): Boolean {
        return thisRef.getBooleanData(data)
    }

    override fun setValue(thisRef: Entity, property: KProperty<*>, value: Boolean) {
        thisRef.setBooleanData(data, value)
    }
}

class Flag(private val flag: EntityFlag): ReadWriteProperty<Entity, Boolean> {
    override fun getValue(thisRef: Entity, property: KProperty<*>): Boolean {
        return thisRef.getFlag(flag)
    }

    override fun setValue(thisRef: Entity, property: KProperty<*>, value: Boolean) {
        thisRef.setFlag(flag, value)
    }
}

class InverseFlag(private val flag: EntityFlag): ReadWriteProperty<Entity, Boolean> {
    override fun getValue(thisRef: Entity, property: KProperty<*>): Boolean {
        return !thisRef.getFlag(flag)
    }

    override fun setValue(thisRef: Entity, property: KProperty<*>, value: Boolean) {
        thisRef.setFlag(flag, !value)
    }
}
