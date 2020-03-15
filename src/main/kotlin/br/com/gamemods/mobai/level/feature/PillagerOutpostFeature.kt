package br.com.gamemods.mobai.level.feature

import br.com.gamemods.mobai.level.spawning.SpawnEntry
import cn.nukkit.entity.EntityTypes

open class PillagerOutpostFeature: StructureEntityFeature {
    override val name = "Pillager_Outpost"
    override val monsterSpawns = mutableListOf(
        SpawnEntry(EntityTypes.PILLAGER, 1, 1, 1)
    )
    override val creatureSpawns = mutableListOf<SpawnEntry>()
}
