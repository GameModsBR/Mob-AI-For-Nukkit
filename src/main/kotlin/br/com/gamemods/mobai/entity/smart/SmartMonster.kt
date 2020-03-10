package br.com.gamemods.mobai.entity.smart

import br.com.gamemods.mobai.entity.SpawnType
import br.com.gamemods.mobai.entity.Spawnable
import br.com.gamemods.mobai.entity.brightnessAtEyes
import br.com.gamemods.mobai.entity.smart.logic.entity
import br.com.gamemods.mobai.level.difficulty
import br.com.gamemods.mobai.level.getBrightness
import br.com.gamemods.mobai.level.getLight
import br.com.gamemods.mobai.math.intFloor
import cn.nukkit.entity.EntityType
import cn.nukkit.level.BlockPosition
import cn.nukkit.level.Level
import cn.nukkit.math.Vector3i
import java.util.*

interface SmartMonster: SmartEntity {
    override val isDisallowedInPeaceful get() = true

    override val safeFallDistance: Int
        get() = if (ai.target == null) {
            3
        } else {
            val entity = entity
            var distance = (entity.health - entity.maxHealth * 0.33f).intFloor()
            distance -= (3 - entity.level.difficulty) * 4
            if (distance < 0) {
                distance = 0
            }
            distance + 3
        }

    override fun pathFindingFavor(pos: BlockPosition): Float {
        return 0.5F - pos.level.getBrightness(pos)
    }

    override fun updateDespawnCounterTick(tickDiff: Int): Boolean {
        if (entity.brightnessAtEyes > 0.5F) {
            despawnCounter += 2
        }
        return super.updateDespawnCounterTick(tickDiff)
    }

    companion object: Spawnable() {
        override fun canSpawn(type: EntityType<*>, level: Level, spawnType: SpawnType, spawnPos: Vector3i, random: Random): Boolean {
            return level.difficulty != 0
                    && isSpawnDark(level, spawnPos, random)
                    && SmartEntity.canSpawn(type, level, spawnType, spawnPos, random)
        }

        fun isSpawnDark(level: Level, spawnPos: Vector3i, random: Random): Boolean {
            if (level.getBlockSkyLightAt(spawnPos.x, spawnPos.y, spawnPos.z) > random.nextInt(32)) {
                return false
            }
            val light = if (level.isThundering) {
                level.getLight(spawnPos, 10)
            } else {
                level.getFullLight(spawnPos)
            }
            return light <= random.nextInt(8)
        }
    }
}
