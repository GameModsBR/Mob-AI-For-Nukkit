package br.com.gamemods.mobai.entity.passive

import br.com.gamemods.mobai.ai.goal.EscapeDangerGoal
import br.com.gamemods.mobai.ai.goal.LookAroundGoal
import br.com.gamemods.mobai.ai.goal.LookAtEntityGoal
import br.com.gamemods.mobai.ai.goal.WanderAroundFarGoal
import br.com.gamemods.mobai.entity.attribute
import br.com.gamemods.mobai.entity.smart.EntityAI
import br.com.gamemods.mobai.entity.smart.EntityProperties
import br.com.gamemods.mobai.entity.smart.EntityPropertyStorage
import br.com.gamemods.mobai.entity.smart.SmartAnimal
import cn.nukkit.entity.Attribute.MAX_HEALTH
import cn.nukkit.entity.Attribute.MOVEMENT_SPEED
import cn.nukkit.entity.EntityType
import cn.nukkit.entity.impl.passive.EntityPig
import cn.nukkit.entity.passive.Pig
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.level.chunk.Chunk
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.player.Player

class SmartPig(type: EntityType<Pig>, chunk: Chunk, tag: CompoundTag)
    : EntityPig(type, chunk, tag), SmartAnimal, EntityProperties by EntityPropertyStorage(tag) {
    override val ai = EntityAI(this).apply {
        goalSelector.add(1, EscapeDangerGoal(this, 1.25))
        goalSelector.add(6, WanderAroundFarGoal(this, 1.0))
        goalSelector.add(7, LookAtEntityGoal(this, Player::class, 6.0))
        goalSelector.add(8, LookAroundGoal(this))
    }

    init {
        initAttributes()
    }

    override fun initAttributes() {
        super.initAttributes()
        attribute(MAX_HEALTH).value = 10F
        attribute(MOVEMENT_SPEED).value = 0.25F
    }

    override fun updateMovement() = super<SmartAnimal>.updateMovement()
    override fun onUpdate(currentTick: Int) = super<SmartAnimal>.onUpdate(currentTick)
    override fun attack(source: EntityDamageEvent) = super<EntityPig>.attack(source) && super<SmartAnimal>.attack(source)
}
