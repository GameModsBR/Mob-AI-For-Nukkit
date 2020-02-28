package br.com.gamemods.mobai.ai.pathing

import br.com.gamemods.mobai.entity.smart.EntityAI

abstract class EntityNavigation(val ai: EntityAI) {
    open val isIdle get() = true

    abstract fun tick(): Boolean
}
