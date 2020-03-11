package br.com.gamemods.mobai.level

import br.com.gamemods.mobai.entity.isFireImmune
import cn.nukkit.block.Block
import cn.nukkit.block.BlockIds.*
import cn.nukkit.block.BlockTrapdoor
import cn.nukkit.entity.EntityType
import cn.nukkit.entity.EntityTypes.*

fun Block.allowsSpawning(type: EntityType<*>): Boolean {
    return when (id) {
        GLASS, STAINED_GLASS, BARRIER, BEDROCK -> false
        INVISIBLE_BEDROCK -> false
        CARVED_PUMPKIN, REDSTONE_LAMP, LIT_REDSTONE_LAMP, SOUL_SAND -> true
        ICE, FROSTED_ICE -> type == POLAR_BEAR
        LEAVES, LEAVES2 -> when (type) {
            OCELOT, PARROT -> true
            else -> false
        }
        MAGMA -> type.isFireImmune
        else -> when(this) {
            is BlockTrapdoor -> false
            else -> lightLevel < 14 && !isTransparent && isSolid
        }
    }
}
