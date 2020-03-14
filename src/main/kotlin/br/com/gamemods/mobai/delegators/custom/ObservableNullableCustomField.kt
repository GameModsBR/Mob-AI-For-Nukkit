package br.com.gamemods.mobai.delegators.custom

import br.com.gamemods.mobai.delegators.observable.ReferencedObservableRWProperty
import kotlin.reflect.KProperty

open class ObservableNullableCustomField<R: Any, T: Any?>(tClass: Class<T>, initialValue: T)
    : NullableCustomField<R, T>(tClass, initialValue), ReferencedObservableRWProperty<R, T?> {
    override fun setValue(thisRef: R, property: KProperty<*>, value: T?) {
        changeValue(thisRef, property, value) {
            super.setValue(thisRef, property, it)
        }
    }
}
