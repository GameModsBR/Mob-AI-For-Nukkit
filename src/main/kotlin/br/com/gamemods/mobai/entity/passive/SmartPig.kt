package br.com.gamemods.mobai.entity.passive

import br.com.gamemods.mobai.ai.goal.LookAroundGoal
import br.com.gamemods.mobai.ai.goal.LookAtEntityGoal
import br.com.gamemods.mobai.entity.smart.EntityAI
import br.com.gamemods.mobai.entity.smart.SmartEntity
import cn.nukkit.entity.EntityType
import cn.nukkit.entity.impl.passive.EntityPig
import cn.nukkit.entity.passive.Pig
import cn.nukkit.level.chunk.Chunk
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.player.Player

class SmartPig(type: EntityType<Pig>, chunk: Chunk, tag: CompoundTag) : EntityPig(type, chunk, tag), SmartEntity {
    override val ai = EntityAI(this).apply {
        goalSelector.add(7, LookAtEntityGoal(this, Player::class, 6.0))
        goalSelector.add(8, LookAroundGoal(this))
    }

    override fun updateMovement() = super<SmartEntity>.updateMovement()
    override fun onUpdate(currentTick: Int) = super<SmartEntity>.onUpdate(currentTick)
}
