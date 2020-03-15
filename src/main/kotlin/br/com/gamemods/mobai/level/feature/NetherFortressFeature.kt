package br.com.gamemods.mobai.level.feature

import br.com.gamemods.mobai.entity.FutureEntityTypes.ZOMBIFIED_PIGLIN
import br.com.gamemods.mobai.level.spawning.SpawnEntry
import cn.nukkit.entity.EntityTypes.*

open class NetherFortressFeature: StructureEntityFeature {
    override val name = "Fortress"
    override val monsterSpawns = mutableListOf(
        SpawnEntry(BLAZE, 10, 2, 3),
        SpawnEntry(ZOMBIFIED_PIGLIN, 5, 4, 4),
        SpawnEntry(WITHER_SKELETON, 8, 5, 5),
        SpawnEntry(SKELETON, 2, 5, 5),
        SpawnEntry(MAGMA_CUBE, 3, 4, 4)
    )
    override val creatureSpawns = mutableListOf<SpawnEntry>()
}
