@file:Suppress("NOTHING_TO_INLINE")

package br.com.gamemods.mobai.math

object MobAiMath {
    inline fun square(value: Double) = value * value
    inline fun clamp(check: Double, min: Double, max: Double): Double {
        return if (check > max) max else if (check < min) min else check
    }

    fun subtractAngles(start: Double, end: Double): Double {
        return wrapDegrees(end - start)
    }

    fun wrapDegrees(d: Double): Double {
        var e = d % 360.0
        if (e >= 180.0) {
            e -= 360.0
        }
        if (e < -180.0) {
            e += 360.0
        }
        return e
    }
}
