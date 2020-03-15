package br.com.gamemods.mobai.level.feature

import br.com.gamemods.mobai.level.spawning.SpawnEntry
import cn.nukkit.entity.EntityTypes
import cn.nukkit.level.Level
import cn.nukkit.math.Vector3i

open class SwampHutFeature: StructureEntityFeature {
    override val name = "Swamp_Hut"
    override val monsterSpawns = mutableListOf(
        SpawnEntry(EntityTypes.WITCH, 1, 1, 1)
    )
    override val creatureSpawns = mutableListOf(
        SpawnEntry(EntityTypes.CAT, 1, 1, 1)
    )

    fun isInsideHut(level: Level, levelPos: Vector3i): Boolean = TODO()
}
