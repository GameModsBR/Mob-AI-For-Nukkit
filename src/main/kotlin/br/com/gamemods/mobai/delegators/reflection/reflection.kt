package br.com.gamemods.mobai.delegators.reflection

import java.lang.reflect.Field
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.cast

inline fun <reified R: Any> intField(name: String) = IntFieldReflection(R::class.java.getDeclaredField(name))
inline fun <reified R: Any, reified T: Any> field(name: String) = FieldReflection(T::class, R::class.java.getDeclaredField(name))

class IntFieldReflection(private val field: Field) {
    init {
        field.isAccessible = true
    }
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return field.getInt(thisRef)
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        field.setInt(thisRef, value)
    }
}

class FieldReflection<T: Any>(private val kClass: KClass<T>, private val field: Field): ReadWriteProperty<Any?, T> {
    init {
        field.isAccessible = true
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return kClass.cast(field[thisRef])
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        field[thisRef] = value
    }
}
