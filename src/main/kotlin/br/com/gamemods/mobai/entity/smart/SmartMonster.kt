package br.com.gamemods.mobai.entity.smart

import br.com.gamemods.mobai.entity.brightnessAtEyes
import br.com.gamemods.mobai.entity.smart.logic.entity
import br.com.gamemods.mobai.level.getBrightness
import br.com.gamemods.mobai.math.intFloor
import cn.nukkit.level.BlockPosition

interface SmartMonster: SmartEntity {
    override val isDisallowedInPeaceful get() = true

    override val safeFallDistance: Int
        get() = if (ai.target == null) {
            3
        } else {
            val entity = entity
            var distance = (entity.health - entity.maxHealth * 0.33f).intFloor()
            distance -= (3 - entity.level.server.difficulty) * 4
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
}
