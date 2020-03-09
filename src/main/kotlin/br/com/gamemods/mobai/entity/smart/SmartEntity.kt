package br.com.gamemods.mobai.entity.smart

import br.com.gamemods.mobai.ExtraAttributeIds.UNDERWATER_MOVEMENT
import br.com.gamemods.mobai.entity.*
import br.com.gamemods.mobai.entity.definition.EntityDefinition
import br.com.gamemods.mobai.entity.definition.EntityPersistence
import br.com.gamemods.mobai.level.get
import br.com.gamemods.mobai.level.isClimbable
import br.com.gamemods.mobai.level.jumpVelocityMultiplier
import br.com.gamemods.mobai.math.MobAiMath
import br.com.gamemods.mobai.nbt.forEach
import br.com.gamemods.mobai.nbt.listen
import cn.nukkit.block.BlockTrapdoor
import cn.nukkit.entity.Attribute.*
import cn.nukkit.entity.Entity
import cn.nukkit.entity.Projectile
import cn.nukkit.entity.data.EntityFlag.*
import cn.nukkit.entity.impl.BaseEntity
import cn.nukkit.entity.impl.EntityLiving
import cn.nukkit.event.entity.EntityDamageByEntityEvent
import cn.nukkit.event.entity.EntityDamageEvent
import cn.nukkit.item.Item
import cn.nukkit.level.BlockPosition
import cn.nukkit.math.MathHelper
import cn.nukkit.math.Vector3f
import cn.nukkit.nbt.NBTIO
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.ListTag
import cn.nukkit.nbt.tag.StringTag
import cn.nukkit.potion.Effect
import kotlin.math.abs

interface SmartEntity: MoveLogic {
    val ai: EntityAI<*>
    var maxHealth: Float
    val equipments get() = ai.equipments

    private inline val entity get() = this as Entity
    private inline val base get() = this as BaseEntity

    val velocityMultiplier: Float get() = entity.defaultVelocityMultiplier
    val jumpVelocity get() = 0.42F * jumpVelocityMultiplier
    val jumpVelocityMultiplier: Float get() {
        val multiplier = base.levelBlock.jumpVelocityMultiplier
        return if (multiplier == 1F) {
            base.level[base.velocityAffectingPos].jumpVelocityMultiplier
        } else {
            multiplier
        }
    }

    @Suppress("RedundantIf")
    val isClimbing: Boolean get() { base.apply {
        val blockPos = position.asVector3i()
        val block = level.getBlock(blockPos)
        return if (block.isClimbable) {
            //climbing = blockPos
            true
        } else if (block is BlockTrapdoor && canEnterTrapdoor(block)) {
            //climbing = blockPos
            true
        } else {
            false
        }
    }}

    fun init() {
        initData()
        initAttributes()
        loadNBT()
    }

    fun initDefinitions() {
        definitions += EntityDefinition(entity.type.identifier)
    }

    fun initData() {
        entity.addFlags(GRAVITY, CAN_CLIMB)
    }

    fun initAttributes() {
        entity.movementSpeed = 0F
        addAttributes(
            KNOCKBACK_RESISTANCE,
            MOVEMENT_SPEED,
            UNDERWATER_MOVEMENT,
            FOLLOW_RANGE,
            ABSORPTION
        )
        addAttribute(healthAttribute)
        entity.apply {
            attribute(ABSORPTION).maxValue = 16F
        }
    }

    fun setMaxHealth(maxHealth: Int)

    fun onUpdate(currentTick: Int): Boolean {
        val entity = base
        healthAttribute.value = entity.health
        healthAttribute.maxValue = maxHealth
        if (entity.closed) {
            return false
        }

        if (!entity.isAlive) {
            /*entity.deadTicks++
            if (entity.deadTicks >= 10) {
                entity.despawnFromAll()
                entity.close()
            }
            return entity.deadTicks < 10*/
            return false
        }

        entity.apply {
            if (justCreated && (!onGround || motionY != 0.0)) {
                val bb = boundingBox.clone()
                bb.minY = bb.minY - 0.75
                onGround = level.getCollisionBlocks(bb).isNotEmpty()
            }
        }

        val tickDiff = currentTick - entity.lastUpdate

        if (tickDiff <= 0) {
            return false
        }

        entity.lastUpdate = currentTick

        val needUpdate = entity.entityBaseTick(tickDiff) or
            updateAttacker() or
            tickMovement(tickDiff)

        entity.updateMovement()
        entity.updateData()
        return needUpdate
    }

    fun updateAttacker(): Boolean {
        val attacker = attacker ?: return false
        if (!attacker.isAlive || base.ticksLived - lastAttackedTime > 100) {
            this.attacker = null
            return false
        }
        return true
    }

    fun attack(source: EntityDamageEvent): Boolean {
        var entity = (source as? EntityDamageByEntityEvent)?.damager ?: return true
        if (entity is Projectile) {
            entity = entity.shooter ?: return true
        }
        attacker = entity
        lastAttackedTime = base.ticksLived
        return true
    }

    fun tickMovement(tickDiff: Int): Boolean {
        var needsUpdate = true

        val entity = base
        if (jumpingCooldown > 0) {
            jumpingCooldown--
        }

        if (isAiDisabled) {
            entity.motion = entity.motion.multiply(0.98)
        }

        val motion = entity.motion
        if (abs(motion.x) < 0.003) {
            motion.x = 0.0
        }
        if (abs(motion.y) < 0.003) {
            motion.y = 0.0
        }
        if (abs(motion.z) < 0.003) {
            motion.z = 0.0
        }

        entity.motion = motion
        if (entity.isDeadOrImmobile) {
            isJumping = false
            sidewaysSpeed = 0F
            forwardSpeed = 0F
        } else if (!isAiDisabled) {
            needsUpdate = needsUpdate or ai.tickAI(tickDiff)
        }

        if (isJumping) {
            val waterHeight: Double
            val bl = if (entity.isTouchingWater) {
                waterHeight = entity.waterHeight
                waterHeight > 0.0
            } else {
                waterHeight = 0.0
                false
            }

            if (bl && (!entity.isOnGround || waterHeight > 0.4)) {
                swimUpWater()
            } else if (entity.isInLava) {
                swimUpLava()
            } else if ((entity.isOnGround || bl && waterHeight <= 0.4) && jumpingCooldown == 0) {
                jump()
                jumpingCooldown = 10
            }
        } else {
            jumpingCooldown = 0
        }

        sidewaysSpeed *= 0.98F
        forwardSpeed *= 0.98F
        // TODO: Skipping initAi, which is actually elytra flying
        //val box = entity.boundingBox.clone()
        travel(Vector3f(sidewaysSpeed.toDouble(), upwardSpeed.toDouble(), forwardSpeed.toDouble()))
        //TODO Remove this debug code
        /*if ((ai.navigation.currentTarget?.distanceSquared(base) ?: 0.0) > 20.square()) {
            ai.navigation.stop()
            base.kill()
        }*/
        //TODO: Skipping push
        //TODO: Skipping tickCramming

        return needsUpdate
    }

    fun mobTick(tickDiff: Int): Boolean {
        return false
    }

    fun swimUpLava() {
        base.motion = base.motion.add(0.0, 0.03999999910593033, 0.0)
    }

    fun swimUpWater() {
        base.motion = base.motion.add(0.0, 0.03999999910593033, 0.0)
    }

    fun jump() { base.apply {
        var jumpVelocity = jumpVelocity
        if (hasEffect(Effect.JUMP)) {
            jumpVelocity += 0.1f * (getEffect(Effect.JUMP).amplifier + 1)
        }
        motion = Vector3f(motionX, jumpVelocity.toDouble(), motionZ)
        if (getFlag(SPRINTING)) {
            val g: Float = this.yaw.toFloat() * 0.017453292f
            motion = motion.add(-MathHelper.sin(g) * 0.2, 0.0, MathHelper.cos(g) * 0.2)
        }
    }}

    fun canTarget(entity: Entity) = entity is EntityLiving

    fun isTeammate(entity: Entity) = false

    fun canSee(entity: Entity): Boolean {
        val id = entity.runtimeId
        if (id in visibleEntityIdsCache) {
            return true
        }
        if (id in invisibleEntityIdsCache) {
            return false
        }
        val canSee = canSeeUncached(entity)
        if (canSee) {
            visibleEntityIdsCache += id
        } else {
            invisibleEntityIdsCache += id
        }
        return canSee
    }

    fun canSeeUncached(entity: Entity): Boolean {
        //TODO
        return true
    }

    fun pathFindingFavor(pos: BlockPosition) = 0F

    fun setPositionAndRotation(pos: Vector3f, yaw: Double, pitch: Double, headYaw: Double): Boolean {
        if (entity.setPositionAndRotation(pos, yaw, pitch)) {
            this.headYaw = headYaw
            return true
        }
        return false
    }

    fun setRotation(yaw: Double, pitch: Double, headYaw: Double) {
        this.headYaw = headYaw
        entity.setRotation(yaw, pitch)
    }

    fun updateMovement() = updateMovementInclHead()
    fun updateMovementInclHead() {
        base.apply {
            val diffPosition = MobAiMath.square(x - lastX) + MobAiMath.square(y - lastY) + MobAiMath.square(z - lastZ)
            val diffRotation = MobAiMath.square(yaw - lastYaw) + MobAiMath.square(pitch - lastPitch)
            val diffHeadRotation = MobAiMath.square(headYaw - lastHeadYaw) + MobAiMath.square(pitch - lastPitch)

            val diffMotion =
                MobAiMath.square(motionX - lastMotionX)
            +MobAiMath.square(motionY - lastMotionY)
            +MobAiMath.square(motionZ - lastMotionZ)

            if (diffPosition > 0.0001 || diffRotation > 1.0 || diffHeadRotation > 1.0) { //0.2 ** 2, 1.5 ** 2
                lastX = x
                lastY = y
                lastZ = z
                lastYaw = yaw
                lastPitch = pitch
                lastHeadYaw = headYaw
                addMovement(x, y, z, yaw, pitch, headYaw)
            }

            if (diffMotion > 0.0025 || diffMotion > 0.0001 && motion.lengthSquared() <= 0.0001) { //0.05 ** 2
                lastMotionX = motionX
                lastMotionY = motionY
                lastMotionZ = motionZ
                addMotion(motionX, motionY, motionZ)
            }
        }
    }

    fun onAttacking(target: Entity) {
        attacking = target
        lastAttackTime = base.ticksLived
    }

    fun tryAttack(target: Entity): Boolean { base.apply {
        return true
        /*var bl: Boolean
        var i: Int
        var f = attribute(ATTACK_DAMAGE).value as Float
        var g = 5F //attribute(ATTACK_KNOCKBACK).value
        if (target is EntityLiving) {
            f += EnchantmentHelper.getAttackDamage(this.getMainHandStack(), (target as LivingEntity).getGroup())
            g += EnchantmentHelper.getKnockback(this) as Float
        }
        if (EnchantmentHelper.getFireAspect(this).also({ i = it }) > 0) {
            target.setOnFireFor(i * 4)
        }
        if (target.damage(DamageSource.mob(this), f).also({ bl = it })) {
            if (g > 0.0f && target is LivingEntity) {
                (target as LivingEntity).takeKnockback(
                    g * 0.5f,
                    MathHelper.sin(this.yaw * 0.017453292f),
                    -MathHelper.cos(this.yaw * 0.017453292f)
                )
                this.setVelocity(this.getVelocity().multiply(0.6, 1.0, 0.6))
            }
            if (target is PlayerEntity) {
                var playerEntity: PlayerEntity
                this.method_24521(
                    playerEntity,
                    this.getMainHandStack(),
                    if ((target as PlayerEntity?. also {
                            playerEntity = it
                        }).isUsingItem()) playerEntity.getActiveItem() else ItemStack.EMPTY
                )
            }
            this.dealDamage(this, target)
            onAttacking(target)
        }
        return bl*/
    }}

    fun isBreedingItem(item: Item) = false

    fun saveNBT() {
        val nbt = base.namedTag
        saveEquipments(nbt)
        saveAttributes(nbt)
        saveDefinitions(nbt)
        saveCommonData(nbt)
        saveSpecificData(nbt)
    }

    fun loadNBT() {
        val nbt = base.namedTag
        loadEquipments(nbt)
        loadAttributes(nbt)
        loadDefinitions(nbt)
        loadCommonData(nbt)
        loadSpecificData(nbt)
    }

    fun loadSpecificData(nbt: CompoundTag) {
    }

    fun saveSpecificData(nbt: CompoundTag) {
    }

    fun saveAttributes(nbt: CompoundTag) {
        ListTag<CompoundTag>("Attributes").apply {
            attributes.values.forEach { attribute ->
                add(CompoundTag()
                    .putFloat("Base", attribute.defaultValue)
                    .putFloat("Current", attribute.value)
                    .putFloat("Max", attribute.maxValue)
                    .putFloat("Min", attribute.minValue)
                )
            }
            nbt.putList(this)
        }
    }

    fun loadAttributes(nbt: CompoundTag) {
        nbt.listenList<CompoundTag>("Attributes") { list ->
            list.forEach {
                val name = it.getString("Name")
                val base = it.getFloat("Base")
                val current = it.getFloat("Current")
                val max = it.getFloat("Max")
                val min = it.getFloat("Min")
                addAttribute(AttributeRegistry.load(name, min, max, base, current))
            }
        }
    }

    fun saveEquipments(nbt: CompoundTag) {
        ListTag<CompoundTag>("Armor").apply {
            add(NBTIO.putItemHelper(equipments.helmet))
            add(NBTIO.putItemHelper(equipments.chestplate))
            add(NBTIO.putItemHelper(equipments.leggings))
            add(NBTIO.putItemHelper(equipments.boots))
            nbt.putList(this)
        }
        ListTag<CompoundTag>("Mainhand").apply {
            add(NBTIO.putItemHelper(equipments.mainHand))
            nbt.putList(this)
        }
        ListTag<CompoundTag>("Offhand").apply {
            add(NBTIO.putItemHelper(equipments.offHand))
            nbt.putList(this)
        }
    }

    fun loadEquipments(nbt: CompoundTag) {
        equipments.clear()
        nbt.listenList<CompoundTag>("Armor") { inv ->
            inv.listen(0) { equipments.helmet = NBTIO.getItemHelper(it) }
            inv.listen(1) { equipments.chestplate = NBTIO.getItemHelper(it) }
            inv.listen(2) { equipments.leggings = NBTIO.getItemHelper(it) }
            inv.listen(3) { equipments.boots = NBTIO.getItemHelper(it) }
        }
        nbt.listenList<CompoundTag>("Mainhand") { inv ->
            inv.listen(0) { equipments.mainHand = NBTIO.getItemHelper(it) }
        }
        nbt.listenList<CompoundTag>("Offhand") { inv ->
            inv.listen(0) { equipments.offHand = NBTIO.getItemHelper(it) }
        }
    }

    fun saveDefinitions(nbt: CompoundTag) {
        ListTag<StringTag>("definitions").apply {
            definitions.forEach {
                add(StringTag("", it.toString()))
            }
            nbt.putList(this)
        }
    }

    fun loadDefinitions(nbt: CompoundTag) {
        definitions.reset()
        nbt.listenList<StringTag>("definitions") { list ->
            list.forEach {
                definitions += EntityDefinition(it.data)
            }
        }
    }

    fun saveCommonData(nbt: CompoundTag) {
        EntityPersistence.saveFlags(entity, nbt)
        EntityPersistence.saveData(entity, nbt)
    }

    fun loadCommonData(nbt: CompoundTag) {
        EntityPersistence.loadFlags(entity, nbt)
        EntityPersistence.loadData(entity, nbt)
    }
}
