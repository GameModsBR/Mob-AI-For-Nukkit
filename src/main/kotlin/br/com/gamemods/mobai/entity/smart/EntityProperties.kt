package br.com.gamemods.mobai.entity.smart

import br.com.gamemods.mobai.ai.pathing.PathNodeType
import br.com.gamemods.mobai.entity.attribute.AttributeModifier
import br.com.gamemods.mobai.entity.definition.EntityDefinitionCollection
import br.com.gamemods.mobai.level.SimpleSound
import cn.nukkit.entity.Attribute
import cn.nukkit.entity.Entity
import cn.nukkit.math.Vector3f
import it.unimi.dsi.fastutil.longs.LongSet
import java.util.*
import java.util.concurrent.ThreadLocalRandom

interface EntityProperties {
    var headYaw: Double
    var lastHeadYaw: Double
    var flyingSpeed: Float
    var sidewaysSpeed: Float
    var upwardSpeed: Float
    var forwardSpeed: Float
    val lookPitchSpeed: Double
    val lookMovingSpeed: Double
    val lookYawSpeed: Double
    val attackDistanceScalingFactor: Double
    val attributes: MutableMap<Int, Attribute>
    val attributeModifiers: MutableMap<AttributeModifier.Operation, MutableMap<UUID, AttributeModifier>>
    val definitions: EntityDefinitionCollection
    val pathFindingPenalties: EnumMap<PathNodeType, Float>
    val stepHeight: Float
    val safeFallDistance: Int
    var isJumping: Boolean
    val visibleEntityIdsCache: LongSet
    val invisibleEntityIdsCache: LongSet
    var jumpingCooldown: Int
    var isAiDisabled: Boolean
    val baseMovementSpeedMultiplier: Float
    var noClip: Boolean
    var movementMultiplier: Vector3f
    var attacker: Entity?
    var lastAttackedTime: Int
    var attacking: Entity?
    var lastAttackTime: Int
    var distanceTraveled: Float
    var nextStepSoundDistance: Float
    var simpleStepSound: SimpleSound?
    val maxHealthAttribute: Attribute
    var expDrop: IntRange
    var loveTicks: Int
    var lovingPlayerId: Long?
    var isPersistent: Boolean
    var breedingAge: Int
    var forcedBreedingAge: Int
    var despawnCounter: Int
    val limitPerChunk get() = 4
    val random: Random get() = ThreadLocalRandom.current()

    val isInLove get() = loveTicks > 0

    fun addAttribute(attribute: Attribute) {
        if (attribute.id == Attribute.MAX_HEALTH) {
            val healthAttribute = maxHealthAttribute
            healthAttribute.minValue = attribute.minValue
            healthAttribute.maxValue = attribute.maxValue
            healthAttribute.defaultValue = attribute.defaultValue
            healthAttribute.value = attribute.value
            attributes[attribute.id] = healthAttribute
        } else {
            attributes[attribute.id] = attribute
        }
    }

    fun addAttribute(id: Int) {
        addAttribute(Attribute.getAttribute(id))
    }

    fun addAttributes(first: Int, vararg others: Int) {
        addAttribute(first)
        others.forEach(this::addAttribute)
    }

    fun pathFindingPenalty(nodeType: PathNodeType) = pathFindingPenalties[nodeType] ?: nodeType.defaultPenalty
}
