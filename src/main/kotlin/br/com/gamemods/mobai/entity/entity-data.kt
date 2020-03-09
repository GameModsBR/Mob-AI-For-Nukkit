package br.com.gamemods.mobai.entity

import cn.nukkit.entity.Entity
import cn.nukkit.entity.data.EntityData
import cn.nukkit.entity.data.EntityFlag
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

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
