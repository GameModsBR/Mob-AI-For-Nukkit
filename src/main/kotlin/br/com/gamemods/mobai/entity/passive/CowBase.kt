package br.com.gamemods.mobai.entity.passive

import br.com.gamemods.mobai.ai.goal.*
import br.com.gamemods.mobai.entity.attribute
import br.com.gamemods.mobai.entity.baseValue
import br.com.gamemods.mobai.entity.lootingLevel
import br.com.gamemods.mobai.entity.smart.EntityAI
import br.com.gamemods.mobai.entity.smart.SmartAnimal
import br.com.gamemods.mobai.entity.smart.logic.Breedable
import br.com.gamemods.mobai.entity.smart.logic.entity
import br.com.gamemods.mobai.level.SimpleSound
import cn.nukkit.entity.Attribute
import cn.nukkit.entity.impl.BaseEntity
import cn.nukkit.entity.passive.Mooshroom
import cn.nukkit.item.Item
import cn.nukkit.item.ItemIds
import cn.nukkit.level.Sound
import cn.nukkit.math.Vector3f
import cn.nukkit.player.Player

interface CowBase: SmartAnimal, Breedable {
    fun <E, AI: EntityAI<E>> AI.initGoals(): AI where E: BaseEntity, E: CowBase, E: Breedable {
        goalSelector.add(0, SwimGoal(this))
        goalSelector.add(1, EscapeDangerGoal(this, 2.0))
        goalSelector.add(2, AnimalMateGoal(this, 1.0))
        goalSelector.add(3, TemptGoal(this, 1.25, ItemIds.WHEAT))
        goalSelector.add(4, FollowParentGoal(this, 1.25))
        goalSelector.add(5, WanderAroundFarGoal(this, 1.0))
        goalSelector.add(6, LookAtEntityGoal(this, Player::class, 6.0))
        goalSelector.add(7, LookAroundGoal(this))
        return this
    }

    override fun initAttributes() {
        super.initAttributes()
        attribute(Attribute.MOVEMENT_SPEED).baseValue = 0.20000000298023224F

        expDrop = 1..3
        simpleStepSound = SimpleSound(Sound.MOB_COW_STEP)
    }

    override fun onInteract(player: Player, item: Item, clickedPos: Vector3f): Boolean {
        when (item.id) {
            ItemIds.BUCKET -> {
                //TODO Milking cow
                return false
            }
            ItemIds.BOWL -> {
                if (this is Mooshroom) {
                    //TODO Mushroom soup
                }
                return false
            }
        }
        return super.onInteract(player, item, clickedPos)
    }

    fun getDrops(): Array<Item> { entity {
        val random = random
        val looting = attacker?.lootingLevel ?: 0
        val drops = mutableListOf<Item>(
            Item.get(
                if (isOnFire) ItemIds.COOKED_BEEF else ItemIds.BEEF,
                0,
                1 + random.nextInt(2 + looting)
            ))
        random.nextInt(3 + looting).takeIf { it > 0 }?.let {
            drops += Item.get(ItemIds.LEATHER, it)
        }
        return drops.toTypedArray()
    }}
}
