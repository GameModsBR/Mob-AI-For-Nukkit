package br.com.gamemods.mobai.delegators.observable

import kotlin.reflect.KProperty

typealias ReferencedObservableListener<R, T> = (thisRef: R, property: KProperty<*>, oldValue: T, newValue: T) -> Unit
inline fun <R, T> observable(initialValue: T, crossinline onChange: ReferencedObservableListener<R, T>)
        = object : ReferencedObservable<R, T>(initialValue) {
    override fun afterChange(thisRef: R, property: KProperty<*>, oldValue: T, originalValue: T, newValue: T) {
        onChange(thisRef, property, oldValue, newValue)
    }
}

typealias ReferencedTransformingListener<R, T> = (thisRef: R, property: KProperty<*>, oldValue: T, newValue: T) -> T
inline fun <R, T> transforming(initialValue: T, crossinline transform: ReferencedTransformingListener<R, T>)
        = object : ReferencedObservable<R, T>(initialValue) {
    override fun transform(thisRef: R, property: KProperty<*>, oldValue: T, newValue: T) = transform(thisRef, property, oldValue, newValue)
}
