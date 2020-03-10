package br.com.gamemods.mobai.entity.passive

import br.com.gamemods.mobai.entity.SpawnType
import br.com.gamemods.mobai.entity.Spawnable
import br.com.gamemods.mobai.entity.passive.SmartMooshroom.MooshroomType.BROWN
import br.com.gamemods.mobai.entity.passive.SmartMooshroom.MooshroomType.RED
import br.com.gamemods.mobai.entity.smart.EntityAI
import br.com.gamemods.mobai.entity.smart.EntityProperties
import br.com.gamemods.mobai.entity.smart.EntityPropertyStorage
import br.com.gamemods.mobai.entity.smart.logic.Breedable
import br.com.gamemods.mobai.level.get
import br.com.gamemods.mobai.level.getBrightness
import br.com.gamemods.mobai.level.getLight
import cn.nukkit.block.BlockIds
import cn.nukkit.entity.Entity
import cn.nukkit.entity.EntityType
import cn.nukkit.entity.impl.passive.EntityMooshroom
import cn.nukkit.entity.misc.LightningBolt
import cn.nukkit.entity.passive.Mooshroom
import cn.nukkit.event.entity.EntityDamageByEntityEvent
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.item.Item
import cn.nukkit.level.BlockPosition
import cn.nukkit.level.Level
import cn.nukkit.level.chunk.Chunk
import cn.nukkit.math.Vector3f
import cn.nukkit.math.Vector3i
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.player.Player
import java.util.*

class SmartMooshroom(type: EntityType<Mooshroom>, chunk: Chunk, tag: CompoundTag)
    : EntityMooshroom(type, chunk, tag), CowBase,
    EntityProperties by EntityPropertyStorage(tag) {

    //TODO Stew Effect
    override val ai = EntityAI(this).initGoals()

    var mooshromType = RED

    init { init() }

    override fun pathFindingFavor(pos: BlockPosition): Float {
        return if (pos.down().block.id == BlockIds.MYCELIUM) {
            10F
        } else {
            pos.level.getBrightness(pos) - 0.5F
        }
    }

    override fun onStruckByLightning(lightningBolt: LightningBolt?) {
        if ((lastDamageCause as? EntityDamageByEntityEvent)?.damager != lightningBolt) {
            mooshromType = if (mooshromType == RED) BROWN else RED
        }

        super.onStruckByLightning(lightningBolt)
    }

    override fun createChild(partner: Breedable): Entity? {
        return super.createChild(partner)?.also {
            if (partner is SmartMooshroom && it is SmartMooshroom) {
                it.mooshromType = chooseBabyType(it, partner)
            }
        }
    }

    fun chooseBabyType(baby: SmartMooshroom, partner: SmartMooshroom): MooshroomType {
        val thisType = mooshromType
        val otherType = partner.mooshromType
        return if (thisType == otherType && random.nextInt(1024) == 0) {
            if (thisType == RED) BROWN else RED
        } else {
            if (random.nextBoolean()) thisType else otherType
        }
    }

    override fun isBreedingItem(item: Item) = super<EntityMooshroom>.isBreedingItem(item)

    override fun getDrops() = super<CowBase>.getDrops()

    override fun onInteract(player: Player, item: Item, clickedPos: Vector3f): Boolean {
        return super<CowBase>.onInteract(player, item, clickedPos)
                || super<EntityMooshroom>.onInteract(player, item, clickedPos)
    }

    override fun updateMovement() = super<CowBase>.updateMovement()
    override fun onUpdate(currentTick: Int) = super<CowBase>.onUpdate(currentTick)
    override fun attack(source: EntityDamageEvent) = super<EntityMooshroom>.attack(source) && super<CowBase>.attack(source)

    override fun saveNBT() {
        super<EntityMooshroom>.saveNBT()
        super<CowBase>.saveNBT()
    }

    override fun kill() {
        super<EntityMooshroom>.kill()
        super<CowBase>.kill()
    }

    override var maxHealth = 20F
    override fun setMaxHealth(maxHealth: Int) {
        super.setMaxHealth(maxHealth)
        this.maxHealth = maxHealth.toFloat()
    }

    enum class MooshroomType {
        RED,
        BROWN
    }

    companion object: Spawnable() {
        override fun canSpawn(type: EntityType<*>, level: Level, spawnType: SpawnType, spawnPos: Vector3i, random: Random): Boolean {
            return level[spawnPos.down()].id == BlockIds.MYCELIUM && level.getLight(spawnPos, 0) > 8
        }
    }
}
