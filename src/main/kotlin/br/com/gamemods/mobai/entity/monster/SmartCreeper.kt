package br.com.gamemods.mobai.entity.monster

import br.com.gamemods.mobai.ai.goal.*
import br.com.gamemods.mobai.entity.Flag
import br.com.gamemods.mobai.entity.IntData
import br.com.gamemods.mobai.entity.attribute
import br.com.gamemods.mobai.entity.baseValue
import br.com.gamemods.mobai.entity.smart.EntityAI
import br.com.gamemods.mobai.entity.smart.EntityProperties
import br.com.gamemods.mobai.entity.smart.EntityPropertyStorage
import br.com.gamemods.mobai.entity.smart.SmartMonster
import br.com.gamemods.mobai.math.clamp
import cn.nukkit.entity.Attribute.MOVEMENT_SPEED
import cn.nukkit.entity.Entity
import cn.nukkit.entity.EntityType
import cn.nukkit.entity.EntityTypes
import cn.nukkit.entity.data.EntityData.FUSE_LENGTH
import cn.nukkit.entity.data.EntityFlag.CHARGED
import cn.nukkit.entity.data.EntityFlag.IGNITED
import cn.nukkit.entity.hostile.Creeper
import cn.nukkit.entity.impl.hostile.EntityCreeper
import cn.nukkit.entity.passive.Cat
import cn.nukkit.entity.passive.Ocelot
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.event.entity.EntityExplosionPrimeEvent
import cn.nukkit.level.Explosion
import cn.nukkit.level.Sound
import cn.nukkit.level.chunk.Chunk
import cn.nukkit.level.gamerule.GameRules
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.player.Player
import cn.nukkit.potion.Effect
import cn.nukkit.registry.EntityRegistry

class SmartCreeper(type: EntityType<Creeper>, chunk: Chunk, nbt: CompoundTag)
        : EntityCreeper(type, chunk, nbt), SmartMonster, EntityProperties by EntityPropertyStorage(nbt) {
    override val ai = EntityAI(this).apply {
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
    var charged by Flag(CHARGED)
    var ignited by Flag(IGNITED)
    var fuseTime = 30
    var headsDropped = 0

    init { init() }

    override fun initData() {
        super.initData()
        currentFuseTime = 0
        charged = false
        ignited = false
    }

    override fun initAttributes() {
        super.initAttributes()
        attribute(MOVEMENT_SPEED).baseValue = 0.25F
    }

    override val safeFallDistance: Int
        get() = if (ai.target == null) 3 else 3 + (health - 1).toInt()

    override fun fall(fallDistance: Float) {
        super.fall(fallDistance)
        currentFuseTime = (currentFuseTime + fallDistance * 1.5F).toInt().coerceAtMost(fuseTime - 5)
    }

    override fun mobTick(tickDiff: Int): Boolean {
        if (!isAlive) {
            return false
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
            EntityRegistry.get().newEntity(EntityTypes.AREA_EFFECT_CLOUD, chunk, Entity.getDefaultNBT(position))
                ?: return
        areaEffectCloud.radius = 2.5f
        //areaEffectCloud.setRadiusOnUse(-0.5f)
        areaEffectCloud.waitTime = 10
        areaEffectCloud.duration /= 2
        //areaEffectCloud.setRadiusGrowth(-areaEffectCloud.getRadius() / areaEffectCloud.getDuration() as Float)
        for (statusEffectInstance in effects) {
            areaEffectCloud.addEffect(statusEffectInstance)
        }
        areaEffectCloud.spawnToAll()
    }

    override fun updateMovement() = super<SmartMonster>.updateMovement()
    override fun onUpdate(currentTick: Int) = super<SmartMonster>.onUpdate(currentTick)
    override fun attack(source: EntityDamageEvent) = super<EntityCreeper>.attack(source) && super<SmartMonster>.attack(source)
}
