package br.com.gamemods.mobai.entity

import cn.nukkit.entity.EntityType
import cn.nukkit.entity.EntityTypes.*

private val fireImmuneEntities = mutableSetOf<EntityType<*>>(
    AREA_EFFECT_CLOUD, BLAZE, ENDER_DRAGON, GHAST,
    MAGMA_CUBE, ZOMBIE_PIGMAN, TNT, VEX, SHULKER,
    WITHER, WITHER_SKELETON
)

val EntityType<*>.isFireImmune get() = this in fireImmuneEntities
fun EntityType<*>.makeFireImmune() {
    fireImmuneEntities += this
}
