package br.com.gamemods.mobai.entity.smart

import br.com.gamemods.mobai.entity.SpawnType
import br.com.gamemods.mobai.entity.Spawnable
import br.com.gamemods.mobai.level.get
import br.com.gamemods.mobai.level.getBrightness
import br.com.gamemods.mobai.level.getLight
import cn.nukkit.block.BlockIds.GRASS
import cn.nukkit.entity.EntityType
import cn.nukkit.level.BlockPosition
import cn.nukkit.level.Level
import cn.nukkit.math.Vector3i
import java.util.*

interface SmartAnimal : SmartEntity {
    override fun initData() {
        super.initData()
        isPersistent = true
    }

    override fun pathFindingFavor(pos: BlockPosition): Float {
        return if (pos.down().block.id == GRASS) {
            10F
        } else {
            pos.level.getBrightness(pos) - 0.5F
        }
    }

    companion object : Spawnable() {
        override fun canSpawn(type: EntityType<*>, level: Level, spawnType: SpawnType, spawnPos: Vector3i, random: Random): Boolean {
            return level[spawnPos.down()].id == GRASS && level.getLight(spawnPos, 0) > 8
        }
    }
}
