package br.com.gamemods.mobai.ai.goal

import br.com.gamemods.mobai.entity.monster.SmartCreeper
import br.com.gamemods.mobai.entity.smart.EntityAI
import cn.nukkit.entity.Entity

class CreeperIgniteGoal(val ai: EntityAI<SmartCreeper>): Goal() {
    private var target: Entity? = null

    init {
        addControls(Control.MOVE)
    }

    override fun canStart(): Boolean {
        if (ai.entity.fuseSpeed > 0) {
            return true
        }

        val target = ai.target ?: return false
        return ai.entity.distanceSquared(target.position) < 9.0
    }

    override fun start() {
        ai.navigation.stop()
        target = ai.target
    }

    override fun stop() {
        target = null
    }

    override fun tick() {
        val target = target
        val creeper = ai.entity
        if (target == null
            || creeper.distanceSquared(target.position) > 49.0
            || !creeper.canSee(target)) {
            creeper.fuseSpeed = -1
            return
        }
        creeper.fuseSpeed = 1
    }
}
