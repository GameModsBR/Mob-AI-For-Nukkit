package br.com.gamemods.mobai.delegators.observable

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface ReferencedObservableRWProperty<R, T>: ReadWriteProperty<R, T> {
    fun beforeChange(thisRef: R, property: KProperty<*>, oldValue: T, newValue: T): Boolean = true

    fun transform(thisRef: R, property: KProperty<*>, oldValue: T, newValue: T): T = newValue

    fun afterChange(thisRef: R, property: KProperty<*>, oldValue: T, originalValue: T, newValue: T) {}
}
