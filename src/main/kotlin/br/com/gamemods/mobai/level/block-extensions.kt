package br.com.gamemods.mobai.level

import cn.nukkit.block.Block
import cn.nukkit.block.BlockIds.*

val Block.isAir get() = when (id) {
    AIR, STRUCTURE_VOID -> true
    else -> false
}

val Block.height: Double get() = boundingBox?.maxY ?: y.toDouble()

val Block.isFlooded get() = id.let { it == WATER || it == FLOWING_WATER } || isWaterlogged

val Block.isClimbable get() = canBeClimbed()

val Block.velocityMultiplier get() = when (id) {
    SOUL_SAND, HONEY_BLOCK -> 0.4F
    else -> 1.0F
}
