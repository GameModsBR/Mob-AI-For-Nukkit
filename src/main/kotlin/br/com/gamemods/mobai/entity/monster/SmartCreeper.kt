package br.com.gamemods.mobai.entity.monster

import br.com.gamemods.mobai.ai.goal.*
import br.com.gamemods.mobai.entity.Flag
import br.com.gamemods.mobai.entity.IntData
import br.com.gamemods.mobai.entity.definition.EntityDefinitionIds.CHARGED_CREEPER
import br.com.gamemods.mobai.entity.definition.EntityDefinitionIds.FORCED_EXPLODING
import br.com.gamemods.mobai.entity.lootingLevel
import br.com.gamemods.mobai.entity.smart.EntityAI
import br.com.gamemods.mobai.entity.smart.EntityProperties
import br.com.gamemods.mobai.entity.smart.EntityPropertyStorage
import br.com.gamemods.mobai.entity.smart.SmartMonster
import br.com.gamemods.mobai.entity.smart.logic.ifNotOnInit
import br.com.gamemods.mobai.math.clamp
import cn.nukkit.entity.Entity
import cn.nukkit.entity.EntityType
import cn.nukkit.entity.EntityTypes
import cn.nukkit.entity.data.EntityData.FUSE_LENGTH
import cn.nukkit.entity.data.EntityFlag
import cn.nukkit.entity.data.EntityFlag.IGNITED
import cn.nukkit.entity.data.EntityFlag.POWERED
import cn.nukkit.entity.hostile.Creeper
import cn.nukkit.entity.hostile.Skeleton
import cn.nukkit.entity.hostile.Stray
import cn.nukkit.entity.impl.hostile.EntityCreeper
import cn.nukkit.entity.misc.LightningBolt
import cn.nukkit.entity.passive.Cat
import cn.nukkit.entity.passive.Ocelot
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.event.entity.EntityExplosionPrimeEvent
import cn.nukkit.item.Item
import cn.nukkit.item.ItemIds.*
import cn.nukkit.item.ItemSkull
import cn.nukkit.level.Explosion
import cn.nukkit.level.Sound
import cn.nukkit.level.chunk.Chunk
import cn.nukkit.level.gamerule.GameRules
import cn.nukkit.math.Vector3f
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.network.protocol.LevelSoundEventPacket
import cn.nukkit.player.Player
import cn.nukkit.potion.Effect
import cn.nukkit.registry.EntityRegistry

class SmartCreeper(type: EntityType<Creeper>, chunk: Chunk, nbt: CompoundTag)
        : EntityCreeper(type, chunk, nbt), SmartMonster, EntityProperties by EntityPropertyStorage(nbt,
    expDrop = 5..5
) {
    override val ai = EntityAI(this).apply {
        goalSelector.add(1, SwimGoal(this))
        goalSelector.add(2, CreeperIgniteGoal(this))
        goalSelector.add(3, FleeEntityGoal(this, Ocelot::class, 6F, 1.0, 1.2))
        goalSelector.add(3, FleeEntityGoal(this, Cat::class, 6F, 1.0, 1.2))
        goalSelector.add(4, MeleeAttackGoal(this, 1.0, false))
        goalSelector.add(5, WanderAroundFarGoal(this, 0.8))
        goalSelector.add(6, LookAtEntityGoal(this, Player::class, 8.0))
        goalSelector.add(6, LookAroundGoal(this))
        targetSelector.add(1, FollowTargetGoal(this, Player::class, true))
        targetSelector.add(2, RevengeGoal(this))
    }

    var fuseSpeed = -1
    var explosionRadius = 3
    var currentFuseTime by IntData(FUSE_LENGTH)
    var ignited by Flag(IGNITED)
    var fuseTime = 30
    private var headsDropped = 0

    init { init() }

    override fun saveSpecificData(nbt: CompoundTag) {
        CompoundTag().apply {
            putByte("FuseSpeed", fuseSpeed)
            putShort("ExplosionRadius", explosionRadius)
            putShort("FuseTime", fuseTime)
            putByte("HeadsDropped", headsDropped)
            nbt.putCompound("SmartCreeper", this)
        }
    }

    override fun loadSpecificData(nbt: CompoundTag) {
        nbt.listenCompound("SmartCreeper") { data ->
            data.listenByte("FuseSpeed") { fuseSpeed = it.toInt() }
            data.listenShort("ExplosionRadius") { explosionRadius = it.toInt() }
            data.listenShort("FuseTime") { fuseTime = it.toInt() }
            data.listenByte("HeadsDropped") { headsDropped = it.toInt() }
        }
    }

    override fun initData() {
        super.initData()
        currentFuseTime = 0
        ignited = false
        if (CHARGED_CREEPER in definitions) {
            isPowered = true
        } else {
            definitions[CHARGED_CREEPER] = isPowered
        }
    }

    override val safeFallDistance: Int
        get() = if (ai.target == null) 3 else 3 + (health - 1).toInt()

    override fun fall(fallDistance: Float) {
        super.fall(fallDistance)
        currentFuseTime = (currentFuseTime + fallDistance * 1.5F).toInt().coerceAtMost(fuseTime - 5)
    }

    fun shouldDropHead() = isPowered && headsDropped <= 0

    fun onHeadDropped() {
        headsDropped++
    }

    override fun mobTick(tickDiff: Int): Boolean {
        if (!isAlive) {
            return false
        }

        if (FORCED_EXPLODING in definitions) {
            fuseSpeed = 1
        }
        ignited = fuseSpeed >= 0

        val speed = fuseSpeed
        if (speed > 0 && currentFuseTime == 0) {
            playSound(Sound.RANDOM_FUSE, 1F, 0.5F)
        }
        currentFuseTime = (currentFuseTime + speed).clamp(0, fuseTime)
        if (currentFuseTime == fuseTime) {
            explode()
        }
        updateData()
        return fuseSpeed >= 0
    }

    fun explode() {
        val event = EntityExplosionPrimeEvent(this, explosionRadius.toDouble())
        event.isBlockBreaking = level.gameRules[GameRules.MOB_GRIEFING]
        server.pluginManager.callEvent(event)
        if (event.isCancelled) {
            return
        }
        val explosion = Explosion(this, event.force, this)
        if (event.isBlockBreaking) {
            explosion.explodeA()
        }
        val effects = effects.values.map(Effect::clone)
        close()
        explosion.explodeB()
        spawnEffectsCloud(effects)
    }

    private fun spawnEffectsCloud(effects: List<Effect>) {
        if (effects.isEmpty()) {
            return
        }

        val areaEffectCloud =
            EntityRegistry.get().newEntity(
                EntityTypes.AREA_EFFECT_CLOUD,
                chunk,
                Entity.getDefaultNBT(position).apply {
                    putFloat("RadiusOnUse", -0.5F)
                    putInt("Duration", 300)
                    putFloat("Radius", 2.5F)
                    putInt("WaitTime", 10)
                    putFloat("RadiusPerTick", -2.5F / 300F)
                }
            ) ?: return
        for (statusEffectInstance in effects) {
            areaEffectCloud.addEffect(statusEffectInstance)
        }
        areaEffectCloud.spawnToAll()
    }

    override fun onInteract(player: Player?, item: Item, clickedPos: Vector3f?): Boolean {
        if (item.id == FLINT_AND_STEEL) {
            item.useOn(this)
            definitions += FORCED_EXPLODING
            level.addLevelSoundEvent(asVector3f(), LevelSoundEventPacket.SOUND_IGNITE, -1, type)
            return true
        }
        return super.onInteract(player, item, clickedPos)
    }

    override fun setFlag(flag: EntityFlag, value: Boolean) {
        if (flag == POWERED) {
            val oldValue = super.setFlag(flag, value)
            ifNotOnInit {
                definitions[CHARGED_CREEPER] = value
            }
            return oldValue
        }
        return super.setFlag(flag, value)
    }

    override fun onStruckByLightning(lightningBolt: LightningBolt?) {
        val bolt = lightningBolt ?: return super.onStruckByLightning(lightningBolt)
        setPowered(bolt)
    }

    override fun getDrops(): Array<Item> {
        val attacker = attacker
        val drops = mutableListOf<Item>()
        val random = random
        val looting = attacker?.lootingLevel ?: 0
        random.nextInt(3 + looting).takeIf { it > 0 }?.let {
            drops += Item.get(GUNPOWDER, 0, it)
        }
        if (attacker is Skeleton || attacker is Stray) {
            drops += arrayOf(
                RECORD_13,
                RECORD_CAT,
                RECORD_BLOCKS,
                RECORD_CHIRP,
                RECORD_FAR,
                RECORD_MALL,
                RECORD_MELLOHI,
                RECORD_STAL,
                RECORD_STRAD,
                RECORD_WARD,
                RECORD_11,
                RECORD_WAIT
            ).let { Item.get(it[random.nextInt(it.size)]) }
        } else if (attacker is SmartCreeper && attacker.shouldDropHead()) {
            attacker.onHeadDropped()
            drops += Item.get(SKULL, ItemSkull.CREEPER_HEAD)
        }
        return drops.toTypedArray()
    }

    override fun setMaxHealth(maxHealth: Int) = super<SmartMonster>.setMaxHealth(maxHealth)
    override fun updateMovement() = super<SmartMonster>.updateMovement()
    override fun onUpdate(currentTick: Int) = super<SmartMonster>.onUpdate(currentTick)
    override fun attack(source: EntityDamageEvent) = super<EntityCreeper>.attack(source) && super<SmartMonster>.attack(source)

    override fun saveNBT() {
        super<EntityCreeper>.saveNBT()
        super<SmartMonster>.saveNBT()
    }

    override fun kill() {
        super<EntityCreeper>.kill()
        super<SmartMonster>.kill()
    }
}
