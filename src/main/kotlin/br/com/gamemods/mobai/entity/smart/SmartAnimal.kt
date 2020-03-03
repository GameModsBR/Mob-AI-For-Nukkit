package br.com.gamemods.mobai.entity.smart

import br.com.gamemods.mobai.level.getBrightness
import cn.nukkit.block.BlockIds.GRASS
import cn.nukkit.level.BlockPosition

interface SmartAnimal : SmartEntity {
    override fun pathFindingFavor(pos: BlockPosition): Float {
        return if (pos.down().block.id == GRASS) {
            10F
        } else {
            pos.level.getBrightness(pos) - 0.5F
        }
    }
}
