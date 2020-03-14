package br.com.gamemods.mobai.delegators.priority

import cn.nukkit.entity.Entity
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.safeCast

class Priority<E: Any, V>(
    private val eClass: KClass<E>,
    private val priority: ReadWriteProperty<E, V>,
    private val fallback: ReadWriteProperty<Entity, V>
): ReadWriteProperty<Entity, V> {

    override fun getValue(thisRef: Entity, property: KProperty<*>): V {
        val e = eClass.safeCast(thisRef)
        return if (e != null) {
            priority.getValue(e, property)
        } else {
            fallback.getValue(thisRef, property)
        }
    }

    override fun setValue(thisRef: Entity, property: KProperty<*>, value: V) {
        val e = eClass.safeCast(thisRef)
        if (e != null) {
            priority.setValue(e, property, value)
        } else {
            fallback.setValue(thisRef, property, value)
        }
    }

}
