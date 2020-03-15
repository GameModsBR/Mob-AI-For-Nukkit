package br.com.gamemods.mobai.level

import cn.nukkit.level.Level

inline class Dimension(val id: Int) {
    inline val isOverworld get() = this == OVERWORLD
    inline val isNether get() = this == NETHER
    inline val isTheEnd get() = this == THE_END

    @Suppress("NOTHING_TO_INLINE")
    companion object {
        inline val OVERWORLD get() = Dimension(Level.DIMENSION_OVERWORLD)
        inline val NETHER get() = Dimension(Level.DIMENSION_NETHER)
        inline val THE_END get() = Dimension(Level.DIMENSION_THE_END)
        inline val MAX get() = THE_END
        inline fun ids() = IntArray(MAX.id + 1) { it }
        inline fun range() = OVERWORLD.id..MAX.id
        inline fun values() = Array(MAX.id + 1) { Dimension(it) }
    }
}
