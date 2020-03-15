@file:Suppress("NOTHING_TO_INLINE")

package br.com.gamemods.mobai

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
