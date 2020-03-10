package br.com.gamemods.mobai.entity.smart

import br.com.gamemods.mobai.ai.pathing.PathNodeType
import br.com.gamemods.mobai.entity.definition.EntityDefinitionCollection
import br.com.gamemods.mobai.level.SimpleSound
import br.com.gamemods.mobai.nbt.firstOrNull
import cn.nukkit.entity.Attribute
import cn.nukkit.entity.Entity
import cn.nukkit.math.Vector3f
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.FloatTag
import it.unimi.dsi.fastutil.longs.LongArraySet
import it.unimi.dsi.fastutil.longs.LongSet
import java.util.*

class EntityPropertyStorage (
    nbt: CompoundTag,
    override var headYaw: Double = nbt.getList("Rotation", FloatTag::class.java).firstOrNull()?.data?.toDouble() ?: 0.0,
    override var lastHeadYaw: Double = headYaw,
    override var flyingSpeed: Float = 0.2F,
    override var sidewaysSpeed: Float = 0F,
    override var upwardSpeed: Float = 0F,
    override var forwardSpeed: Float = 0F,
    override val lookPitchSpeed: Double = 40.0,
    override val lookMovingSpeed: Double = 75.0,
    override val lookYawSpeed: Double = 10.0,
    override val attackDistanceScalingFactor: Double = 1.0,
    override val attributes: MutableMap<Int, Attribute> = mutableMapOf(),
    override val definitions: EntityDefinitionCollection = EntityDefinitionCollection(),
    override val pathFindingPenalties: EnumMap<PathNodeType, Float> = EnumMap<PathNodeType, Float>(PathNodeType::class.java),
    override val stepHeight: Float = 0.6F,
    override val safeFallDistance: Int = 3,
    override var isJumping: Boolean = false,
    override val visibleEntityIdsCache: LongSet = LongArraySet(),
    override val invisibleEntityIdsCache: LongSet = LongArraySet(),
    override var jumpingCooldown: Int = 0,
    override var isAiDisabled: Boolean = false,
    override val baseMovementSpeedMultiplier: Float = 0.8F,
    override var noClip: Boolean = false,
    override var movementMultiplier: Vector3f = Vector3f(),
    override var attacker: Entity? = null,
    override var lastAttackedTime: Int = 0,
    override var attacking: Entity? = null,
    override var lastAttackTime: Int = 0,
    override var distanceTraveled: Float = 0F,
    override var nextStepSoundDistance: Float = 0F,
    override var simpleStepSound: SimpleSound? = null,
    override val healthAttribute: Attribute = Attribute.getAttribute(Attribute.MAX_HEALTH),
    override var expDrop: IntRange = IntRange.EMPTY,
    override var loveTicks: Int = 0,
    override var lovingPlayerId: Long? = null,
    override var isPersistent: Boolean = false,
    override var breedingAge: Int = 0,
    override var forcedBreedingAge: Int = 0,
    override var despawnCounter: Int = 0
) : EntityProperties
