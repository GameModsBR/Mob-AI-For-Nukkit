@file:Suppress("NOTHING_TO_INLINE")

package br.com.gamemods.mobai.math

import cn.nukkit.math.NukkitMath

inline fun Int.square() = this * this
inline fun Float.square() = this * this
inline fun Double.square() = this * this

inline fun Int.clamp(min: Int, max: Int) = if (this > max) max else if (this < min) min else this
inline fun Float.clamp(min: Float, max: Float) = if (this > max) max else if (this < min) min else this
inline fun Double.clamp(min: Double, max: Double) = if (this > max) max else if (this < min) min else this

inline fun Float.intFloor() = NukkitMath.floorFloat(this)
inline fun Double.intFloor() = NukkitMath.floorDouble(this)

inline fun Float.floor() = kotlin.math.floor(this)
inline fun Double.floor() = kotlin.math.floor(this)

inline fun Float.intCeil() = NukkitMath.ceilFloat(this)
inline fun Double.intCeil() = NukkitMath.ceilDouble(this)

inline fun Float.ceil() = kotlin.math.ceil(this)
inline fun Double.ceil() = kotlin.math.ceil(this)
