package br.com.gamemods.mobai.ai.goal

import br.com.gamemods.mobai.ai.goal.Goal.Control.LOOK
import br.com.gamemods.mobai.ai.goal.Goal.Control.MOVE
import br.com.gamemods.mobai.entity.smart.EntityAI
import kotlin.math.cos
import kotlin.math.sin

class LookAroundGoal(private val ai: EntityAI): Goal() {
    private var deltaX = 0.0
    private var deltaZ = 0.0
    private var lookTime = 0

    init {
        addControls(MOVE, LOOK)
    }

    override fun canStart() = ai.random.nextFloat() < 0.02F
    override fun shouldContinue() = lookTime >= 0

    override fun start() {
        val random = ai.random
        val d: Double = Math.PI * 2 * random.nextDouble()
        deltaX = cos(d)
        deltaZ = sin(d)
        lookTime = 20 + random.nextInt(20)
    }

    override fun tick() {
        lookTime--
        val entity = ai.entity
        ai.lookControl.lookAt(entity.x + deltaX, entity.y + entity.eyeHeight, entity.z + deltaZ)
    }
}
