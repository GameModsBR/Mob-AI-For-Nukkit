package br.com.gamemods.mobai.ai.filter

import br.com.gamemods.mobai.entity.isRiding
import cn.nukkit.entity.Entity

object EntityFilters {
    fun doesNotRides(vehicle: Entity) = { entity: Entity ->
        !entity.isRiding(vehicle)
    }
}
