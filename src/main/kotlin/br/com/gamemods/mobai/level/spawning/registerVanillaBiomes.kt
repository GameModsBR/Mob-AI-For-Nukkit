package br.com.gamemods.mobai.level.spawning

import br.com.gamemods.mobai.MobAIPlugin
import br.com.gamemods.mobai.entity.EntityCategory.*
import br.com.gamemods.mobai.entity.FutureEntityTypes.FOX
import br.com.gamemods.mobai.entity.FutureEntityTypes.PIGLIN
import br.com.gamemods.mobai.entity.FutureEntityTypes.ZOMBIFIED_PIGLIN
import cn.nukkit.entity.EntityTypes
import cn.nukkit.entity.EntityTypes.*
import cn.nukkit.level.biome.Biome
import cn.nukkit.level.biome.EnumBiome
import cn.nukkit.level.biome.EnumBiome.*

private typealias EntityType = EntityTypes

private fun Biome.addBat() = addSpawn(AMBIENT, SpawnEntry(BAT, 10, 8, 8))

private fun Biome.addBatAndNormalMonsters() {
    addBat()
    addSpawn(MONSTER,
        SpawnEntry(EntityType.SPIDER, 100, 4, 4),
        SpawnEntry(EntityType.ZOMBIE, 95, 4, 4),
        SpawnEntry(EntityType.ZOMBIE_VILLAGER, 5, 1, 1),
        SpawnEntry(EntityType.SKELETON, 100, 4, 4),
        SpawnEntry(EntityType.CREEPER, 100, 4, 4),
        SpawnEntry(EntityType.SLIME, 100, 4, 4),
        SpawnEntry(EntityType.ENDERMAN, 10, 1, 4),
        SpawnEntry(EntityType.WITCH, 5, 1, 1)
    )
}

internal fun MobAIPlugin.registerVanillaBiomes() {
    logger.debug("Registering biomes")
    EnumBiome.values().forEach { enum ->
        when (enum) {
            OCEAN -> with(enum.biome) {
                addSpawn(WATER_CREATURE,
                    SpawnEntry(SQUID, 1, 1, 4),
                    SpawnEntry(COD, 10, 3, 6),
                    SpawnEntry(DOLPHIN, 1, 1, 2)
                )
                addSpawn(MONSTER, SpawnEntry(EntityType.DROWNED, 5, 1, 1))
                addBatAndNormalMonsters()
            }
            PLAINS, SUNFLOWER_PLAINS -> with(enum.biome) {
                addSpawn(CREATURE,
                    SpawnEntry(SHEEP, 12, 4, 4),
                    SpawnEntry(PIG, 10, 4, 4),
                    SpawnEntry(CHICKEN, 10, 4, 4),
                    SpawnEntry(COW, 8, 4, 4),
                    SpawnEntry(HORSE, 5, 2, 6),
                    SpawnEntry(DONKEY, 1, 1, 3)
                )
                addBatAndNormalMonsters()
            }
            DESERT, DESERT_HILLS, DESERT_M -> with(enum.biome) {
                addSpawn(CREATURE, SpawnEntry(RABBIT, 4, 2, 3))
                addBat()
                addSpawn(MONSTER,
                    SpawnEntry(SPIDER, 100, 4, 4),
                    SpawnEntry(SKELETON, 100, 4, 4),
                    SpawnEntry(CREEPER, 100, 4, 4),
                    SpawnEntry(SLIME, 100, 4, 4),
                    SpawnEntry(ENDERMAN, 10, 1, 4),
                    SpawnEntry(WITCH, 5, 1, 1),
                    SpawnEntry(ZOMBIE, 19, 4, 4),
                    SpawnEntry(ZOMBIE_VILLAGER, 1, 1, 1),
                    SpawnEntry(HUSK, 80, 4, 4)
                )
            }
            EXTREME_HILLS, EXTREME_HILLS_PLUS, EXTREME_HILLS_PLUS_M, EXTREME_HILLS_M -> with(enum.biome) {
                addSpawn(CREATURE,
                    SpawnEntry(SHEEP, 12, 4, 4),
                    SpawnEntry(PIG, 10, 4, 4),
                    SpawnEntry(CHICKEN, 10, 4, 4),
                    SpawnEntry(COW, 8, 4, 4),
                    SpawnEntry(LLAMA, 5, 4, 6)
                )
                addBatAndNormalMonsters()
            }
            FOREST -> with(enum.biome) {
                addSpawn(CREATURE,
                    SpawnEntry(SHEEP, 12, 4, 4),
                    SpawnEntry(PIG, 10, 4, 4),
                    SpawnEntry(CHICKEN, 10, 4, 4),
                    SpawnEntry(COW, 8, 4, 4),
                    SpawnEntry(WOLF, 5, 4, 4)
                )
                addBatAndNormalMonsters()
            }
            TAIGA, TAIGA_HILLS, COLD_TAIGA, COLD_TAIGA_HILLS, COLD_TAIGA_M, MEGA_SPRUCE_TAIGA, TAIGA_M -> with(enum.biome) {
                addSpawn(CREATURE,
                    SpawnEntry(SHEEP, 12, 4, 4),
                    SpawnEntry(PIG, 10, 4, 4),
                    SpawnEntry(CHICKEN, 10, 4, 4),
                    SpawnEntry(COW, 8, 4, 4),
                    SpawnEntry(WOLF, 8, 4, 4),
                    SpawnEntry(RABBIT, 4, 2, 3),
                    SpawnEntry(FOX, 8, 2, 4)
                )
                addBatAndNormalMonsters()
            }
            SWAMP, SWAMPLAND_M -> with(enum.biome) {
                addSpawn(CREATURE,
                    SpawnEntry(SHEEP, 12, 4, 4),
                    SpawnEntry(PIG, 10, 4, 4),
                    SpawnEntry(CHICKEN, 10, 4, 4),
                    SpawnEntry(COW, 8, 4, 4)
                )
                addBatAndNormalMonsters()
                addSpawn(MONSTER, SpawnEntry(SLIME, 1, 1, 1))
            }
            RIVER -> with(enum.biome) {
                addSpawn(WATER_CREATURE,
                    SpawnEntry(SQUID, 2, 1, 4),
                    SpawnEntry(SALMON, 5, 1, 5)
                )
                addBatAndNormalMonsters()
                addSpawn(MONSTER, SpawnEntry(DROWNED, 100, 1, 1))
            }
            HELL -> with(enum.biome) {
                addSpawn(MONSTER,
                    SpawnEntry(GHAST, 50, 4, 4),
                    SpawnEntry(ZOMBIFIED_PIGLIN, 100, 4, 4),
                    SpawnEntry(MAGMA_CUBE, 2, 4, 4),
                    SpawnEntry(ENDERMAN, 1, 4, 4),
                    SpawnEntry(PIGLIN, 15, 4, 4)
                )
            }
            FROZEN_OCEAN -> with(enum.biome) {
                addSpawn(WATER_CREATURE,
                    SpawnEntry(SQUID, 1, 1, 4),
                    SpawnEntry(SALMON, 15, 1, 5)
                )
                addSpawn(CREATURE, SpawnEntry(POLAR_BEAR, 1, 1, 2))
                addBatAndNormalMonsters()
                addSpawn(MONSTER, SpawnEntry(EntityType.DROWNED, 5, 1, 1))
            }
            FROZEN_RIVER -> with(enum.biome) {
                addSpawn(WATER_CREATURE,
                    SpawnEntry(SQUID, 2, 1, 4),
                    SpawnEntry(SALMON, 5, 1, 5)
                )
                addBatAndNormalMonsters()
                addSpawn(MONSTER, SpawnEntry(EntityType.DROWNED, 1, 1, 1))
            }
            ICE_PLAINS, ICE_PLAINS_SPIKES -> with(enum.biome) {
                addSpawn(CREATURE,
                    SpawnEntry(RABBIT, 10, 2, 3),
                    SpawnEntry(POLAR_BEAR, 1, 1, 2)
                )
                addBat()
                addSpawn(MONSTER,
                    SpawnEntry(SPIDER, 100, 4, 4),
                    SpawnEntry(ZOMBIE, 95, 4, 4),
                    SpawnEntry(ZOMBIE_VILLAGER, 5, 1, 1),
                    SpawnEntry(CREEPER, 100, 4, 4),
                    SpawnEntry(SLIME, 100, 4, 4),
                    SpawnEntry(ENDERMAN, 10, 1, 4),
                    SpawnEntry(WITCH, 5, 1, 1),
                    SpawnEntry(SKELETON, 20, 4, 4),
                    SpawnEntry(STRAY, 80, 4, 4)
                )
            }
            MUSHROOM_ISLAND, MUSHROOM_ISLAND_SHORE -> with(enum.biome) {
                addSpawn(CREATURE, SpawnEntry(MOOSHROOM, 8, 4, 8))
                addBat()
            }
            BEACH -> with(enum.biome) {
                addSpawn(CREATURE, SpawnEntry(TURTLE, 5, 2, 5))
                addBatAndNormalMonsters()
            }
            FOREST_HILLS -> with(enum.biome) {
                addSpawn(CREATURE,
                    SpawnEntry(SHEEP, 12, 4, 4),
                    SpawnEntry(PIG, 10, 4, 4),
                    SpawnEntry(CHICKEN, 10, 4, 4),
                    SpawnEntry(COW, 8, 4, 4)
                )
                addBatAndNormalMonsters()
            }
            EXTREME_HILLS_EDGE -> with(enum.biome) {
                addSpawn(CREATURE,
                    SpawnEntry(SHEEP, 12, 4, 4),
                    SpawnEntry(PIG, 10, 4, 4),
                    SpawnEntry(CHICKEN, 10, 4, 4),
                    SpawnEntry(COW, 8, 4, 4),
                    SpawnEntry(LLAMA, 5, 4, 6)
                )
                addBatAndNormalMonsters()
            }
            JUNGLE -> with(enum.biome) {
                addSpawn(CREATURE,
                    SpawnEntry(SHEEP, 12, 4, 4),
                    SpawnEntry(PIG, 10, 4, 4),
                    SpawnEntry(CHICKEN, 10, 4, 4),
                    SpawnEntry(COW, 8, 4, 4),
                    SpawnEntry(PARROT, 40, 1, 2),
                    SpawnEntry(PANDA, 1, 1, 2),
                    SpawnEntry(CHICKEN, 10, 4, 4)
                )
                addBatAndNormalMonsters()
                addSpawn(MONSTER, SpawnEntry(OCELOT, 2, 1, 3))
            }
            JUNGLE_HILLS -> with(enum.biome) {
                addSpawn(CREATURE,
                    SpawnEntry(SHEEP, 12, 4, 4),
                    SpawnEntry(PIG, 10, 4, 4),
                    SpawnEntry(CHICKEN, 10, 4, 4),
                    SpawnEntry(COW, 8, 4, 4),
                    SpawnEntry(PARROT, 10, 1, 1),
                    SpawnEntry(PANDA, 1, 1, 2),
                    SpawnEntry(CHICKEN, 10, 4, 4)
                )
                addBatAndNormalMonsters()
                addSpawn(MONSTER, SpawnEntry(OCELOT, 2, 1, 1))
            }
            JUNGLE_EDGE, JUNGLE_EDGE_M -> with(enum.biome) {
                addSpawn(CREATURE,
                    SpawnEntry(SHEEP, 12, 4, 4),
                    SpawnEntry(PIG, 10, 4, 4),
                    SpawnEntry(CHICKEN, 10, 4, 4),
                    SpawnEntry(COW, 8, 4, 4),
                    SpawnEntry(CHICKEN, 10, 4, 4)
                )
                addBatAndNormalMonsters()
            }
            DEEP_OCEAN -> with(enum.biome) {
                addSpawn(WATER_CREATURE,
                    SpawnEntry(SQUID, 1, 1, 4),
                    SpawnEntry(COD, 10, 3, 6),
                    SpawnEntry(DOLPHIN, 1, 1, 2)
                )
                addBatAndNormalMonsters()
                addSpawn(MONSTER, SpawnEntry(EntityType.DROWNED, 5, 1, 1))
            }
            STONE_BEACH, COLD_BEACH -> with(enum.biome) {
                addBatAndNormalMonsters()
            }
            BIRCH_FOREST, BIRCH_FOREST_HILLS, BIRCH_FOREST_HILLS_M, BIRCH_FOREST_M -> with(enum.biome) {
                addSpawn(CREATURE,
                    SpawnEntry(SHEEP, 12, 4, 4),
                    SpawnEntry(PIG, 10, 4, 4),
                    SpawnEntry(CHICKEN, 10, 4, 4),
                    SpawnEntry(COW, 8, 4, 4)
                )
                addBatAndNormalMonsters()
            }
            ROOFED_FOREST, ROOFED_FOREST_M -> with(enum.biome) {
                addSpawn(CREATURE,
                    SpawnEntry(SHEEP, 12, 4, 4),
                    SpawnEntry(PIG, 10, 4, 4),
                    SpawnEntry(CHICKEN, 10, 4, 4),
                    SpawnEntry(COW, 8, 4, 4),
                    SpawnEntry(WOLF, 5, 4, 4)
                )
                addBatAndNormalMonsters()
            }
            MEGA_TAIGA, MEGA_TAIGA_HILLS -> with(enum.biome) {
                addSpawn(CREATURE,
                    SpawnEntry(SHEEP, 12, 4, 4),
                    SpawnEntry(PIG, 10, 4, 4),
                    SpawnEntry(CHICKEN, 10, 4, 4),
                    SpawnEntry(COW, 8, 4, 4),
                    SpawnEntry(WOLF, 8, 4, 4),
                    SpawnEntry(RABBIT, 4, 2, 3),
                    SpawnEntry(FOX, 8, 2, 4)
                )
                addBat()
                addSpawn(MONSTER,
                    SpawnEntry(SPIDER, 100, 4, 4),
                    SpawnEntry(ZOMBIE, 100, 4, 4),
                    SpawnEntry(SKELETON, 100, 4, 4),
                    SpawnEntry(ZOMBIE_VILLAGER, 25, 1, 1),
                    SpawnEntry(CREEPER, 100, 4, 4),
                    SpawnEntry(SLIME, 100, 4, 4),
                    SpawnEntry(ENDERMAN, 10, 1, 4),
                    SpawnEntry(WITCH, 5, 1, 1)
                )
            }
            SAVANNA, SAVANNA_M, SAVANNA_PLATEAU_M -> with(enum.biome) {
                addSpawn(CREATURE,
                    SpawnEntry(SHEEP, 12, 4, 4),
                    SpawnEntry(PIG, 10, 4, 4),
                    SpawnEntry(CHICKEN, 10, 4, 4),
                    SpawnEntry(COW, 8, 4, 4),
                    SpawnEntry(HORSE, 1, 2, 6),
                    SpawnEntry(DONKEY, 1, 1, 1)
                )
                addBatAndNormalMonsters()
            }
            SAVANNA_PLATEAU -> with(enum.biome) {
                addSpawn(CREATURE,
                    SpawnEntry(SHEEP, 12, 4, 4),
                    SpawnEntry(PIG, 10, 4, 4),
                    SpawnEntry(CHICKEN, 10, 4, 4),
                    SpawnEntry(COW, 8, 4, 4),
                    SpawnEntry(HORSE, 1, 2, 6),
                    SpawnEntry(DONKEY, 1, 1, 1),
                    SpawnEntry(LLAMA, 8, 4, 4)
                )
                addBatAndNormalMonsters()
            }
            MESA, MESA_PLATEAU, MESA_PLATEAU_F, MESA_PLATEAU_F_M, MESA_PLATEAU_M, MESA_BRYCE -> with(enum.biome) {
                addBatAndNormalMonsters()
            }
            FLOWER_FOREST -> with(enum.biome) {
                addSpawn(CREATURE,
                    SpawnEntry(SHEEP, 12, 4, 4),
                    SpawnEntry(PIG, 10, 4, 4),
                    SpawnEntry(CHICKEN, 10, 4, 4),
                    SpawnEntry(COW, 8, 4, 4),
                    SpawnEntry(RABBIT, 4, 2, 3)
                )
                addBatAndNormalMonsters()
            }
            JUNGLE_M -> with(enum.biome) {
                addSpawn(CREATURE,
                    SpawnEntry(SHEEP, 12, 4, 4),
                    SpawnEntry(PIG, 10, 4, 4),
                    SpawnEntry(CHICKEN, 10, 4, 4),
                    SpawnEntry(COW, 8, 4, 4),
                    SpawnEntry(PARROT, 10, 1, 1),
                    SpawnEntry(CHICKEN, 10, 4, 4)
                )
                addBatAndNormalMonsters()
                addSpawn(MONSTER, SpawnEntry(EntityType.OCELOT, 2, 1, 1))
            }
        }
    }
}
