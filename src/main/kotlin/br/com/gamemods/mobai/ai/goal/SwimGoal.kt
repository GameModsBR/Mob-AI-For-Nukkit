package br.com.gamemods.mobai.ai.goal

import br.com.gamemods.mobai.entity.isInLava
import br.com.gamemods.mobai.entity.isTouchingWater
import br.com.gamemods.mobai.entity.smart.EntityAI
import br.com.gamemods.mobai.entity.smart.SmartEntity
import br.com.gamemods.mobai.entity.waterHeight
import cn.nukkit.entity.impl.BaseEntity

class SwimGoal<E>(val ai: EntityAI<E>): Goal() where E: BaseEntity, E: SmartEntity {
    init {
        addControls(Control.JUMP)
        ai.navigation.nodeMaker.canSwim = true
    }

    override fun canStart(): Boolean {
        ai.entity.apply {
            return isInLava || isTouchingWater && waterHeight > if (eyeHeight < 0.4) 0.2 else 0.4
        }
    }

    override fun tick() {
        if (ai.entity.random.nextFloat() < 0.8F) {
            ai.jumpControl.setActive()
        }
    }
}
