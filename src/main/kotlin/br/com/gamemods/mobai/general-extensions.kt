@file:Suppress("NOTHING_TO_INLINE")

package br.com.gamemods.mobai

import co.aikar.timings.Timing
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

inline fun <T: Any> T?.notNull(): T = checkNotNull(this)
inline fun <T: Any> T?.notNull(lazyMessage: () -> String): T {
    contract { callsInPlace(lazyMessage, InvocationKind.AT_MOST_ONCE) }
    return checkNotNull(this, lazyMessage)
}
inline fun <T: Any> T?.notNull(message: String): T = checkNotNull(this) { message }

inline fun <T: Any> T?.requireNotNull(): T = requireNotNull(this)
inline fun <T: Any> T?.requireNotNull(lazyMessage: () -> String): T {
    contract { callsInPlace(lazyMessage, InvocationKind.AT_MOST_ONCE) }
    return requireNotNull(this, lazyMessage)
}
inline fun <T: Any> T?.requireNotNull(message: String): T = requireNotNull(this) { message }

inline fun <T: Any> T.nullable(): T? = this

fun <T> Class<T>.safeCast(obj: Any?) = if (isInstance(obj)) cast(obj) else null

inline fun <T: Any> T?.orThrow(exception: () -> Throwable) = this ?: throw exception()
inline fun <T: Any> T?.orThrow(exception: Throwable) = this ?: throw exception

inline fun <T: Any, reified O> T?.cast() = this as O
inline fun <T: Any, reified O> T?.castOrNull() = this as? O

inline fun Timing.track(func: () -> Unit) {
    contract { callsInPlace(func, InvocationKind.EXACTLY_ONCE) }
    try {
        startTiming()
        func()
    } finally {
        stopTiming()
    }
}

inline fun <R> Timing.runTracking(func: () -> R): R {
    contract { callsInPlace(func, InvocationKind.EXACTLY_ONCE) }
    try {
        startTiming()
        return func()
    } finally {
        stopTiming()
    }
}
