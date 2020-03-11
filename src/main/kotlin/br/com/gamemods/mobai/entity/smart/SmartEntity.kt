package br.com.gamemods.mobai.entity.smart

import br.com.gamemods.mobai.entity.SpawnType
import br.com.gamemods.mobai.entity.Spawnable
import br.com.gamemods.mobai.entity.smart.logic.*
import br.com.gamemods.mobai.level.allowsSpawning
import br.com.gamemods.mobai.level.get
import cn.nukkit.entity.EntityType
import cn.nukkit.level.ChunkManager
import cn.nukkit.level.Level
import cn.nukkit.math.Vector3i
import java.util.*

interface SmartEntity: VisibilityLogic, UpdateLogic, InitLogic, DespawnLogic, CombatLogic {
    companion object : Spawnable() {
        override fun canSpawn(
            type: EntityType<*>,
            level: Level,
            chunkManager: ChunkManager,
            spawnType: SpawnType,
            spawnPos: Vector3i,
            random: Random
        ): Boolean {
            val posDown = spawnPos.down()
            return spawnType == SpawnType.SPAWNER || chunkManager[posDown].allowsSpawning(type)
        }
    }
}
