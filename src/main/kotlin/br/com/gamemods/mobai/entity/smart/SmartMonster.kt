package br.com.gamemods.mobai.entity.smart

import br.com.gamemods.mobai.level.getBrightness
import br.com.gamemods.mobai.math.intFloor
import cn.nukkit.entity.Entity
import cn.nukkit.entity.impl.BaseEntity
import cn.nukkit.level.BlockPosition

interface SmartMonster: SmartEntity {
    override fun pathFindingFavor(pos: BlockPosition): Float {
        return 0.5F - pos.level.getBrightness(pos)
    }

    private inline val entity get() = this as Entity
    private inline val base get() = this as BaseEntity

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
}
