package br.com.gamemods.mobai.entity.passive

import br.com.gamemods.mobai.entity.smart.EntityAI
import br.com.gamemods.mobai.entity.smart.EntityProperties
import br.com.gamemods.mobai.entity.smart.EntityPropertyStorage
import cn.nukkit.entity.EntityType
import cn.nukkit.entity.impl.passive.EntityCow
import cn.nukkit.entity.passive.Cow
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.item.Item
import cn.nukkit.level.chunk.Chunk
import cn.nukkit.math.Vector3f
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.player.Player

class SmartCow(type: EntityType<Cow>, chunk: Chunk, tag: CompoundTag)
    : EntityCow(type, chunk, tag), CowBase,
    EntityProperties by EntityPropertyStorage(tag) {

    override val ai = EntityAI(this).initGoals()

    init { init() }

    override fun isBreedingItem(item: Item) = super<EntityCow>.isBreedingItem(item)

    override fun getDrops() = super<CowBase>.getDrops()

    override fun onInteract(player: Player, item: Item, clickedPos: Vector3f): Boolean {
        return super<CowBase>.onInteract(player, item, clickedPos)
                || super<EntityCow>.onInteract(player, item, clickedPos)
    }

    override fun setMaxHealth(maxHealth: Int) = super<CowBase>.setMaxHealth(maxHealth)
    override fun updateMovement() = super<CowBase>.updateMovement()
    override fun onUpdate(currentTick: Int) = super<CowBase>.onUpdate(currentTick)
    override fun attack(source: EntityDamageEvent) = super<EntityCow>.attack(source) && super<CowBase>.attack(source)

    override fun saveNBT() {
        super<EntityCow>.saveNBT()
        super<CowBase>.saveNBT()
    }

    override fun kill() {
        super<EntityCow>.kill()
        super<CowBase>.kill()
    }
}
