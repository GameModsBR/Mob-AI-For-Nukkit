package br.com.gamemods.mobai.ai.pathing

import br.com.gamemods.mobai.entity.smart.EntityAI

class WalkingNavigation(ai: EntityAI) : EntityNavigation(ai) {
    override fun tick(): Boolean {
        return false
    }
}
