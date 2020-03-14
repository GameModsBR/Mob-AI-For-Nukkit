package br.com.gamemods.mobai.delegators.custom

import br.com.gamemods.mobai.delegators.observable.ReferencedObservableListener
import br.com.gamemods.mobai.delegators.observable.ReferencedObservableRWProperty
import br.com.gamemods.mobai.delegators.observable.ReferencedTransformingListener
import java.util.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

private val customFields = WeakHashMap<Any, MutableMap<KProperty<*>, Any?>>()

inline fun <R: Any, reified T: Any> customField(initialValue: T) = NotNullCustomField<R, T>(T::class.java, initialValue)

@JvmName("customFieldObserving")
inline fun <R: Any, reified T: Any> customField(initialValue: T, crossinline observing: ReferencedObservableListener<R, T>)
        = object : ObservableNotNullCustomField<R, T>(T::class.java, initialValue) {
    override fun afterChange(thisRef: R, property: KProperty<*>, oldValue: T, originalValue: T, newValue: T) {
        observing(thisRef, property, oldValue, newValue)
    }
}

@JvmName("customFieldTransforming")
inline fun <R: Any, reified T: Any> customField(initialValue: T, crossinline transforming: ReferencedTransformingListener<R, T>)
        = object : ObservableNotNullCustomField<R, T>(T::class.java, initialValue) {
    override fun transform(thisRef: R, property: KProperty<*>, oldValue: T, newValue: T): T {
        return transforming(thisRef, property, oldValue, newValue)
    }
}


inline fun <R: Any, reified T: Any?> nullableCustomField(initialValue: T) = NullableCustomField<R, T>(T::class.java, initialValue)

@JvmName("nullableCustomFieldObserving")
inline fun <R: Any, reified T: Any?> nullableCustomField(initialValue: T, crossinline observing: ReferencedObservableListener<R, T?>)
        = object : ObservableNullableCustomField<R, T>(T::class.java, initialValue) {
    override fun afterChange(thisRef: R, property: KProperty<*>, oldValue: T?, originalValue: T?, newValue: T?) {
        observing(thisRef, property, oldValue, newValue)
    }
}

@JvmName("nullableCustomFieldTransforming")
inline fun <R: Any, reified T: Any?> nullableCustomField(initialValue: T, crossinline transforming: ReferencedTransformingListener<R, T?>)
        = object : ObservableNullableCustomField<R, T>(T::class.java, initialValue) {
    override fun transform(thisRef: R, property: KProperty<*>, oldValue: T?, newValue: T?): T? {
        return transforming(thisRef, property, oldValue, newValue)
    }
}

private inline fun <R, T> ReferencedObservableRWProperty<R, T>.changeValue(
    thisRef: R, property: KProperty<*>, value: T, setter: (transformed: T) -> Unit
) {
    synchronized(customFields) {
        val oldValue = getValue(thisRef, property)
        if (!beforeChange(thisRef, property, oldValue, value)) {
            return
        }
        val transformed = transform(thisRef, property, oldValue, value)
        setter(transformed)
        afterChange(thisRef, property, oldValue, value, transformed)
    }
}

open class ObservableNotNullCustomField<R: Any, T: Any>(tClass: Class<T>, initialValue: T)
    : NotNullCustomField<R, T>(tClass, initialValue), ReferencedObservableRWProperty<R, T> {
    override fun setValue(thisRef: R, property: KProperty<*>, value: T) {
        changeValue(thisRef, property, value) {
            super.setValue(thisRef, property, it)
        }
    }
}

open class ObservableNullableCustomField<R: Any, T: Any?>(tClass: Class<T>, initialValue: T)
    : NullableCustomField<R, T>(tClass, initialValue), ReferencedObservableRWProperty<R, T?> {
    override fun setValue(thisRef: R, property: KProperty<*>, value: T?) {
        changeValue(thisRef, property, value) {
            super.setValue(thisRef, property, it)
        }
    }
}

open class NotNullCustomField<R: Any, T: Any>(protected val tClass: Class<T>, protected val initialValue: T)
    : ReadWriteProperty<R, T> {
    override fun getValue(thisRef: R, property: KProperty<*>): T {
        synchronized(customFields) {
            val value = customFields.getOrPut(thisRef, ::mutableMapOf)[property] ?: return initialValue
            return tClass.cast(value)
        }
    }

    override fun setValue(thisRef: R, property: KProperty<*>, value: T) {
        synchronized(customFields) {
            if (value === initialValue) {
                customFields[thisRef]?.remove(property)
            } else {
                customFields.getOrPut(thisRef, ::mutableMapOf)[property] = value
            }
        }
    }
}

open class NullableCustomField<R: Any, T: Any?>(protected val tClass: Class<T>, protected val initialValue: T):
    ReadWriteProperty<R, T?> {
    override fun getValue(thisRef: R, property: KProperty<*>): T? {
        synchronized(customFields) {
            val thisFields = customFields.getOrPut(thisRef, ::mutableMapOf)
            if (property !in thisFields) return initialValue
            val value = thisFields[property] ?: return null
            return tClass.cast(value)
        }
    }

    override fun setValue(thisRef: R, property: KProperty<*>, value: T?) {
        synchronized(customFields) {
            if (value === initialValue) {
                customFields[thisRef]?.remove(property)
            } else {
                customFields.getOrPut(thisRef, ::mutableMapOf)[property] = value
            }
        }
    }
}
