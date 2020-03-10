package br.com.gamemods.mobai.entity.smart.logic

import cn.nukkit.entity.Entity

interface VisibilityLogic: SplitLogic {
    fun canSee(entity: Entity): Boolean {
        val id = entity.runtimeId
        if (id in visibleEntityIdsCache) {
            return true
        }
        if (id in invisibleEntityIdsCache) {
            return false
        }
        val canSee = canSeeUncached(entity)
        if (canSee) {
            visibleEntityIdsCache += id
        } else {
            invisibleEntityIdsCache += id
        }
        return canSee
    }

    fun canSeeUncached(entity: Entity): Boolean {
        //TODO
        return true
    }
}
