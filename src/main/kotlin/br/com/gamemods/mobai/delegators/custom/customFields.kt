package br.com.gamemods.mobai.delegators.custom

import br.com.gamemods.mobai.delegators.observable.ReferencedObservableListener
import br.com.gamemods.mobai.delegators.observable.ReferencedObservableRWProperty
import br.com.gamemods.mobai.delegators.observable.ReferencedTransformingListener
import java.util.*
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

abstract class CustomFieldHandler<R: Any> {
    protected fun <O> usingThisFields(thisRef: R, function: (MutableMap<KProperty<*>, Any?>) -> O): O {
        return synchronized(customFields) {
            val thisFields = customFields.getOrPut(thisRef, ::mutableMapOf)
            function(thisFields)
        }
    }

    protected fun <O> usingThisFieldsIfPresent(thisRef: R, function: (thisFields: MutableMap<KProperty<*>, Any?>?) -> O): O {
        return synchronized(customFields) {
            val thisFields = customFields[thisRef]
            function(thisFields)
        }
    }

    protected fun setOrRemoveFieldValue(thisRef: R, property: KProperty<*>, initialValue: Any?, newValue: Any?) {
        synchronized(customFields) {
            if (newValue === initialValue) {
                customFields[thisRef]?.remove(property)
            } else {
                customFields.getOrPut(thisRef, ::mutableMapOf)[property] = newValue
            }
        }
    }

    protected fun <R, T> ReferencedObservableRWProperty<R, T>.changeValue(
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
}
