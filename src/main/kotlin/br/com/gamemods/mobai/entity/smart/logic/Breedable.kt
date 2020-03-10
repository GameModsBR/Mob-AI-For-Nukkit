package br.com.gamemods.mobai.entity.smart.logic

import br.com.gamemods.mobai.entity.isBaby
import br.com.gamemods.mobai.entity.smart.SmartEntity
import br.com.gamemods.mobai.level.ParticleManager
import br.com.gamemods.mobai.level.doMobLoot
import cn.nukkit.entity.Entity
import cn.nukkit.item.Item
import cn.nukkit.math.Vector3f
import cn.nukkit.player.Player
import cn.nukkit.registry.EntityRegistry

interface Breedable: SplitLogic {
    val isBreedable get() = true
    var lovingPlayer: Player?
        get() = lovingPlayerId?.let { entity.level.getEntity(it) } as? Player
        set(value) {
            lovingPlayerId = value?.uniqueId
        }

    fun canBreedWith(other: Entity): Boolean { entity {
        return this !== other
                && other is Breedable
                && other.isBreedable
                && type == other.type
                && isInLove && other.isInLove
    }}

    fun createChild(partner: Breedable): Entity? { base {
        return EntityRegistry.get().newEntity(type, chunk, Entity.getDefaultNBT(position)).also {
            it.isBaby = true
            if (it is SmartEntity) {
                it.breedingAge = -24000
            }
        }
    }}

    fun breed(partner: Breedable) {
        val baby = createChild(partner) ?: return
        //TODO Breeding achievement
        //val lovingPlayer = lovingPlayer ?: (partner as? Breedable)?.lovingPlayer
        breedingAge = 6000
        partner.breedingAge = 6000
        loveTicks = 0
        partner.loveTicks = 0
        ParticleManager.createHearts(smart)
        if (level.doMobLoot) {
            level.dropExpOrb(entity.position, random.nextInt(7) + 1)
        }
        baby.spawnToAll()
    }

    fun isBreedingItem(item: Item): Boolean = false

    fun onInteract(player: Player, item: Item, clickedPos: Vector3f): Boolean {
        if (!isBreedingItem(item)) {
            return false
        }

        if (breedingAge == 0 && canEat()) {
            eat(player, item)
            lovePlayer(player)
            //TODO swing hand
            return true
        }

        if (entity.isBaby) {
            eat(player, item)
            growUp(((-breedingAge / 20) * 0.1F).toInt(), true)
            return true
        }
        return false
    }

    fun growUp(age: Int, overGrow: Boolean = false) {
        var i: Int
        val j = breedingAge
        i = j
        i += age * 2
        if (i > 0) {
            i = 0
        }
        val k = i - j
        breedingAge = i
        if (overGrow) {
            forcedBreedingAge += k
        }
        if (breedingAge == 0) {
            breedingAge = forcedBreedingAge
        }
    }

    fun eat(player: Player, item: Item) {
        if (!player.isCreative) {
            item.decrementCount()
        }
    }

    fun lovePlayer(player: Player?) {
        loveTicks = 600
        if (player != null) {
            lovingPlayer = player
        }
        ParticleManager.createHearts(smart)
    }

    fun canEat() = loveTicks <= 0
}
