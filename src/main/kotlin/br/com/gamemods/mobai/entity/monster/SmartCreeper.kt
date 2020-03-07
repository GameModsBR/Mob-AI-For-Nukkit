package br.com.gamemods.mobai.entity.monster

import br.com.gamemods.mobai.ai.goal.*
import br.com.gamemods.mobai.entity.attribute
import br.com.gamemods.mobai.entity.smart.EntityAI
import br.com.gamemods.mobai.entity.smart.EntityProperties
import br.com.gamemods.mobai.entity.smart.EntityPropertyStorage
import br.com.gamemods.mobai.entity.smart.SmartMonster
import cn.nukkit.entity.Attribute.MOVEMENT_SPEED
import cn.nukkit.entity.EntityType
import cn.nukkit.entity.hostile.Creeper
import cn.nukkit.entity.impl.hostile.EntityCreeper
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.level.chunk.Chunk
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.player.Player

class SmartCreeper(type: EntityType<Creeper>, chunk: Chunk, nbt: CompoundTag)
        : EntityCreeper(type, chunk, nbt), SmartMonster, EntityProperties by EntityPropertyStorage(nbt) {
    override val ai = EntityAI(this).apply {
        goalSelector.add(4, MeleeAttackGoal(this, 1.0, false))
        goalSelector.add(5, WanderAroundFarGoal(this, 0.8))
        goalSelector.add(6, LookAtEntityGoal(this, Player::class, 8.0))
        goalSelector.add(6, LookAroundGoal(this))
        targetSelector.add(1, FollowTargetGoal(this, Player::class, true))
    }

    init {
        initAttributes()
    }

    override fun initAttributes() {
        super.initAttributes()
        attribute(MOVEMENT_SPEED).value = 0.25F
    }

    override val safeFallDistance: Int
        get() = if (ai.target == null) 3 else 3 + (health - 1).toInt()

    override fun updateMovement() = super<SmartMonster>.updateMovement()
    override fun onUpdate(currentTick: Int) = super<SmartMonster>.onUpdate(currentTick)
    override fun attack(source: EntityDamageEvent) = super<EntityCreeper>.attack(source) && super<SmartMonster>.attack(source)
}
