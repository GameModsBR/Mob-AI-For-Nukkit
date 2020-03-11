package br.com.gamemods.mobai.entity.smart

import br.com.gamemods.mobai.entity.SpawnType
import br.com.gamemods.mobai.entity.Spawnable
import br.com.gamemods.mobai.entity.group.PassiveGroupData
import br.com.gamemods.mobai.entity.isBaby
import br.com.gamemods.mobai.entity.smart.logic.entity
import br.com.gamemods.mobai.level.getBlockIdAt
import br.com.gamemods.mobai.level.getBrightness
import br.com.gamemods.mobai.level.getLight
import cn.nukkit.block.BlockIds.GRASS
import cn.nukkit.entity.EntityType
import cn.nukkit.level.BlockPosition
import cn.nukkit.level.ChunkManager
import cn.nukkit.level.Level
import cn.nukkit.math.Vector3i
import java.util.*

interface SmartAnimal : SmartEntity {
    override fun canDespawnImmediately(distanceSquared: Double): Boolean {
        return false
    }

    override fun pathFindingFavor(pos: BlockPosition): Float {
        return if (pos.down().block.id == GRASS) {
            10F
        } else {
            pos.level.getBrightness(pos) - 0.5F
        }
    }

    override fun postSpawn(spawnType: SpawnType, groupData: Any?, random: Random): Any? {
        val passiveData = groupData as? PassiveGroupData ?: PassiveGroupData()
        if (passiveData.isBabyAllowed
            && passiveData.spawnCount > 0
            && random.nextFloat() <= passiveData.babyChance) {
            entity {
                breedingAge = -24000
                isBaby = true
            }
        }
        passiveData.spawnCount++
        return super.postSpawn(spawnType, passiveData, random)
    }

    companion object : Spawnable() {
        override fun canSpawn(
            type: EntityType<*>,
            level: Level,
            chunkManager: ChunkManager,
            spawnType: SpawnType,
            spawnPos: Vector3i,
            random: Random
        ): Boolean {
            return chunkManager.getBlockIdAt(spawnPos.down()) == GRASS && chunkManager.getLight(spawnPos, 0) > 8
        }
    }
}
