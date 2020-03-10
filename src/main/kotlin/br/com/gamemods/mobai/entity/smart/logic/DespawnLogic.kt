package br.com.gamemods.mobai.entity.smart.logic

import br.com.gamemods.mobai.level.closest
import br.com.gamemods.mobai.level.difficulty
import br.com.gamemods.mobai.level.findPlayers
import br.com.gamemods.mobai.math.square

interface DespawnLogic: SplitLogic {
    val isDisallowedInPeaceful get() = false

    fun checkDespawn() { base {
        if (level.difficulty == 0 && isDisallowedInPeaceful) {
            close()
            return
        }
        if (isPersistent || cannotDespawn()) {
            despawnCounter = 0
            return
        }
        val (_, distanceSquared) = level.findPlayers(this).closest() ?: return
        if (distanceSquared > 128.square() && canDespawnImmediately(distanceSquared)) {
            close()
            return
        }
        if (despawnCounter > 600
            && random.nextInt(800) == 0
            && distanceSquared > 32.square()
            && canDespawnImmediately(distanceSquared)) {
            close()
            return
        }

        if (distanceSquared < 32.square()) {
            despawnCounter = 0
        }
    }}

    fun cannotDespawn() = false
    fun canDespawnImmediately(distanceSquared: Double) = true
}
