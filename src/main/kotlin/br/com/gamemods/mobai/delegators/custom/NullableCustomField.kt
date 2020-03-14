package br.com.gamemods.mobai.delegators.custom

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

open class NullableCustomField<R: Any, T: Any?>(protected val tClass: Class<T>, protected val initialValue: T)
    : ReadWriteProperty<R, T?>, CustomFieldHandler<R>() {
    override fun getValue(thisRef: R, property: KProperty<*>): T? {
        return usingThisFieldsIfPresent(thisRef) { thisFields ->
            if (thisFields?.contains(property) == true) {
                thisFields[property]?.let(tClass::cast)
            } else {
                initialValue
            }
        }
    }

    override fun setValue(thisRef: R, property: KProperty<*>, value: T?) {
        setOrRemoveFieldValue(thisRef, property, initialValue, value)
    }
}
