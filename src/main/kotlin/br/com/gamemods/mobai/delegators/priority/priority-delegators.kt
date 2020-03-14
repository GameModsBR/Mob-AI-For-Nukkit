package br.com.gamemods.mobai.delegators.priority

import cn.nukkit.entity.Entity
import kotlin.properties.ReadWriteProperty

inline fun <reified E: Any, V> priority(main: ReadWriteProperty<E, V>, fallback: ReadWriteProperty<Entity, V>) =
    Priority(E::class, main, fallback)
inline fun <reified E: Any, V> priority(noinline mainGetter: E.() -> V, noinline mainSetter: E.(V) -> Unit, fallback: ReadWriteProperty<Entity, V>)
        = Priority(
    E::class,
    SimpleReadWriteProperty(mainGetter, mainSetter),
    fallback
)
inline fun <reified E: Any, V> priority(main: ReadWriteProperty<E, V>, noinline fallbackGetter: Entity.() -> V, noinline fallbackSetter: Entity.(V) -> Unit)
        = Priority(
    E::class,
    main,
    SimpleReadWriteProperty(fallbackGetter, fallbackSetter)
)
inline fun <reified E: Any, V> priority(noinline mainGetter: E.() -> V, noinline mainSetter: E.(V) -> Unit, noinline fallbackGetter: Entity.() -> V, noinline fallbackSetter: Entity.(V) -> Unit)
        = Priority(
    E::class,
    SimpleReadWriteProperty(mainGetter, mainSetter),
    SimpleReadWriteProperty(fallbackGetter, fallbackSetter)
)

