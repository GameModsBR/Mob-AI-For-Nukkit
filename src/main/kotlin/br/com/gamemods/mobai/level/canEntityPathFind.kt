package br.com.gamemods.mobai.level

import br.com.gamemods.mobai.ai.pathing.PathFindEnvironment
import br.com.gamemods.mobai.level.FutureBlockIds.CRIMSON_FUNGUS
import br.com.gamemods.mobai.level.FutureBlockIds.CRIMSON_ROOTS
import br.com.gamemods.mobai.level.FutureBlockIds.NETHER_SPROUTS
import br.com.gamemods.mobai.level.FutureBlockIds.WARPED_FUNGUS
import br.com.gamemods.mobai.level.FutureBlockIds.WARPED_ROOTS
import cn.nukkit.block.*
import cn.nukkit.block.BlockIds.*
import cn.nukkit.entity.Entity

fun Block.canEntityPathFind(@Suppress("UNUSED_PARAMETER") entity: Entity, environment: PathFindEnvironment): Boolean {
    when (id) {
        ANVIL, BAMBOO, BED, BELL, BREWING_STAND, CACTUS, CAKE, CAMPFIRE,
            CAULDRON, LAVA_CAULDRON, CHEST, CHORUS_PLANT, COMPOSTER, CONDUIT,
            DRAGON_EGG, ENCHANTING_TABLE, ENDER_CHEST, END_PORTAL_FRAME,
            FARMLAND, FENCE, LAVA, FLOWING_LAVA, GLASS_PANE, GRINDSTONE,
            HOPPER, LANTERN, LECTERN, PISTON, STICKY_PISTON, PISTON_ARM_COLLISION,
            STICKY_PISTON_ARM_COLLISION, MOVING_BLOCK, SOUL_SAND, STONECUTTER, STONECUTTER_BLOCK,
            NETHER_SPROUTS
            -> return false
        WATER, FLOWING_WATER -> return true
        DEADBUSH, BROWN_MUSHROOM, RED_MUSHROOM, SEAGRASS, WITHER_ROSE, SEA_PICKLE,
            TALL_GRASS, SWEET_BERRY_BUSH, NETHER_WART, WATERLILY,
            CRIMSON_ROOTS, WARPED_ROOTS, CRIMSON_FUNGUS, WARPED_FUNGUS
            -> return plantEntityPathFind(environment)
        SNOW_LAYER -> return snowLayerEntityPathFind(environment)
    }
    return when (this) {
        is BlockFlower, is BlockDoublePlant, is BlockCrops, is BlockSapling -> plantEntityPathFind(environment)
        is BlockDoor -> doorEntityPathFind(environment)
        is BlockFenceGate -> fenceGateEntityPathFind(environment)
        is BlockFence, is BlockGlassPane, is BlockStairs, is BlockWall -> false
        is BlockSlab -> slabEntityPathFind(environment)
        is BlockTrapdoor -> trapdoorEntityPathFind(environment)
        else -> defaultEntityPathFindRules(environment)
    }
}

private fun BlockTrapdoor.trapdoorEntityPathFind(environment: PathFindEnvironment) = when (environment) {
    PathFindEnvironment.LAND, PathFindEnvironment.AIR -> isOpen
    PathFindEnvironment.WATER -> isWaterlogged
}

private fun Block.snowLayerEntityPathFind(environment: PathFindEnvironment) = when (environment) {
    PathFindEnvironment.LAND -> damage < 5
    else -> false
}

private fun BlockSlab.slabEntityPathFind(environment: PathFindEnvironment) = when (environment) {
    PathFindEnvironment.WATER -> isWaterlogged
    else -> false
}

private fun Block.plantEntityPathFind(environment: PathFindEnvironment): Boolean {
    if (environment == PathFindEnvironment.AIR) {
        return canPassThrough()
    }
    return defaultEntityPathFindRules(environment)
}

private fun BlockFenceGate.fenceGateEntityPathFind(environment: PathFindEnvironment) = when (environment) {
    PathFindEnvironment.LAND, PathFindEnvironment.AIR -> isOpen
    else -> false
}

private fun BlockDoor.doorEntityPathFind(environment: PathFindEnvironment) = when (environment) {
    PathFindEnvironment.LAND, PathFindEnvironment.AIR -> isOpen
    else -> false
}

private fun Block.defaultEntityPathFindRules(environment: PathFindEnvironment) = when (environment) {
    PathFindEnvironment.LAND, PathFindEnvironment.AIR -> !isSolid
    PathFindEnvironment.WATER -> id.let { it == WATER || it == FLOWING_WATER } || isWaterlogged
}
