package br.com.gamemods.mobai.ai.control

import br.com.gamemods.mobai.entity.smart.EntityAI
import br.com.gamemods.mobai.entity.smart.SmartEntity
import cn.nukkit.entity.impl.BaseEntity

open class JumpControl<E>(ai: EntityAI<E>) where E: SmartEntity, E: BaseEntity {
    val entity = ai.entity
    var active = false; protected set

    fun setActive() {
        active = true
    }

    fun tick(): Boolean {
        entity.isJumping = active
        active = false
        return true
    }
}
