package br.com.gamemods.mobai.delegators

import cn.nukkit.entity.Entity
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.safeCast

inline fun <reified E: Any, V> priority(main: ReadWriteProperty<E, V>, fallback: ReadWriteProperty<Entity, V>) = Priority(E::class, main, fallback)
inline fun <reified E: Any, V> priority(noinline mainGetter: E.() -> V, noinline mainSetter: E.(V) -> Unit, fallback: ReadWriteProperty<Entity, V>)
        = Priority(E::class, SimpleReadWriteProperty(mainGetter, mainSetter), fallback)
inline fun <reified E: Any, V> priority(main: ReadWriteProperty<E, V>, noinline fallbackGetter: Entity.() -> V, noinline fallbackSetter: Entity.(V) -> Unit)
        = Priority(E::class, main, SimpleReadWriteProperty(fallbackGetter, fallbackSetter))
inline fun <reified E: Any, V> priority(noinline mainGetter: E.() -> V, noinline mainSetter: E.(V) -> Unit, noinline fallbackGetter: Entity.() -> V, noinline fallbackSetter: Entity.(V) -> Unit)
        = Priority(E::class, SimpleReadWriteProperty(mainGetter, mainSetter), SimpleReadWriteProperty(fallbackGetter, fallbackSetter))

class SimpleReadWriteProperty<E, V>(val getter: E.() -> V, val setter: E.(V) -> Unit): ReadWriteProperty<E, V> {
    override fun getValue(thisRef: E, property: KProperty<*>): V {
        return getter(thisRef)
    }

    override fun setValue(thisRef: E, property: KProperty<*>, value: V) {
        setter(thisRef, value)
    }
}

class NoOp<V>(private val default: V): ReadWriteProperty<Any?, V> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): V {
        return default
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: V) {
    }
}

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
