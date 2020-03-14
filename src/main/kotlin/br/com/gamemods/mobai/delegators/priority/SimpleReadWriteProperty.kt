package br.com.gamemods.mobai.delegators.priority

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SimpleReadWriteProperty<E, V>(val getter: E.() -> V, val setter: E.(V) -> Unit): ReadWriteProperty<E, V> {
    override fun getValue(thisRef: E, property: KProperty<*>): V {
        return getter(thisRef)
    }

    override fun setValue(thisRef: E, property: KProperty<*>, value: V) {
        setter(thisRef, value)
    }
}
