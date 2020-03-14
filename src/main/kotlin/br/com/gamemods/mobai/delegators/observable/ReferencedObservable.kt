package br.com.gamemods.mobai.delegators.observable

import kotlin.reflect.KProperty

abstract class ReferencedObservable<R, T>(initialValue: T): ReferencedObservableRWProperty<R, T> {
    protected var value = initialValue

    override fun getValue(thisRef: R, property: KProperty<*>): T {
        return value
    }

    override fun setValue(thisRef: R, property: KProperty<*>, value: T) {
        val oldValue = this.value
        if (!beforeChange(thisRef, property, oldValue, value)) {
            return
        }
        val transformed = transform(thisRef, property, oldValue, value)
        this.value = transformed
        afterChange(thisRef, property, oldValue, value, transformed)
    }
}
