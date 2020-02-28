package br.com.gamemods.mobai.entity.smart

interface SmartEntity {
    val ai: EntityAI

    fun updateMovement() = ai.updateMovementInclHead()
    fun onUpdate(currentTick: Int) = ai.onSmartEntityUpdate(currentTick)
}
