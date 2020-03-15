package br.com.gamemods.mobai.level.feature

import br.com.gamemods.mobai.level.spawning.SpawnEntry
import cn.nukkit.entity.EntityTypes

open class OceanMonumentFeature: StructureEntityFeature {
    override val name = "Monument"
    override val monsterSpawns = mutableListOf(
        SpawnEntry(EntityTypes.GUARDIAN, 1, 2, 4)
    )
    override val creatureSpawns = mutableListOf<SpawnEntry>()
}
