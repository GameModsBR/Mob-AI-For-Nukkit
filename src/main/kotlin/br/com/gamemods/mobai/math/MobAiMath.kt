@file:Suppress("NOTHING_TO_INLINE")

package br.com.gamemods.mobai.math

object MobAiMath {
    const val SQUARE_ROOT_OF_TWO_F = 1.4142135f
    const val SQUARE_ROOT_OF_TWO_D = 1.4142135623730951
    const val RAD2DEG_F = 57.2957763671875

    inline fun square(value: Double) = value.square()
    inline fun clamp(check: Double, min: Double, max: Double) = check.clamp(min, max)

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

    fun wrapDegrees(f: Float): Float {
        var g = f % 360.0f
        if (g >= 180.0f) {
            g -= 360.0f
        }
        if (g < -180.0f) {
            g += 360.0f
        }
        return g
    }

    fun horizontalChunkBlock(pos: Int) = pos and 0x0F
}
