package br.com.gamemods.mobai.delegators.priority

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class NoOp<V>(private val default: V): ReadWriteProperty<Any?, V> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): V {
        return default
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: V) {
    }
}
