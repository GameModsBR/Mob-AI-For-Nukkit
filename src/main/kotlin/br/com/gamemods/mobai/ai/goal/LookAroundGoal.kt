package br.com.gamemods.mobai.ai.goal

import br.com.gamemods.mobai.ai.goal.Goal.Control.LOOK
import br.com.gamemods.mobai.ai.goal.Goal.Control.MOVE
import br.com.gamemods.mobai.entity.smart.EntityAI
import br.com.gamemods.mobai.entity.smart.SmartEntity
import cn.nukkit.entity.impl.BaseEntity
import cn.nukkit.math.MathHelper

class LookAroundGoal<E>(ai: EntityAI<E>): Goal() where E: SmartEntity, E: BaseEntity {
    private val entity = ai.entity
    private var deltaX = 0.0
    private var deltaZ = 0.0
    private var lookTime = 0

    init {
        addControls(MOVE, LOOK)
    }

    override fun canStart() = entity.random.nextFloat() < 0.02F
    override fun shouldContinue() = lookTime >= 0

    override fun start() {
        val random = entity.random
        val d: Double = Math.PI * 2 * random.nextDouble()
        deltaX = MathHelper.cos(d).toDouble()
        deltaZ = MathHelper.sin(d).toDouble()
        lookTime = 20 + random.nextInt(20)
    }

    override fun tick() {
        lookTime--
        this.entity.ai.lookControl.lookAt(entity.x + deltaX, entity.y + entity.eyeHeight, entity.z + deltaZ)
    }
}
