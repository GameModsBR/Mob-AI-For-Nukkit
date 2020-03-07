package br.com.gamemods.mobai.entity.smart

import br.com.gamemods.mobai.ai.pathing.PathNodeType
import cn.nukkit.entity.Attribute
import cn.nukkit.math.Vector3f
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.FloatTag
import it.unimi.dsi.fastutil.longs.LongArraySet
import it.unimi.dsi.fastutil.longs.LongSet
import java.util.*
import java.util.concurrent.ThreadLocalRandom

class EntityPropertyStorage (
    override var headYaw: Double,
    override var lastHeadYaw: Double = headYaw,
    override var flyingSpeed: Float = 0.2F,
    override var sidewaysSpeed: Float = 0F,
    override var upwardSpeed: Float = 0F,
    override var forwardSpeed: Float = 0F,
    override var deSpawnCounter: Int = 0,
    override val lookPitchSpeed: Double = 40.0,
    override val lookMovingSpeed: Double = 75.0,
    override val lookYawSpeed: Double = 10.0,
    override val attackDistanceScalingFactor: Double = 1.0,
    override val attributes: MutableMap<Int, Attribute> = mutableMapOf(),
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
    override var movementMultiplier: Vector3f = Vector3f()
    //override var climbing: Vector3i? = null
) : EntityProperties {

    constructor(nbt: CompoundTag) : this(
        nbt.getList("Rotation", FloatTag::class.java)[0].data.toDouble()
    )
}

interface EntityProperties {
    var headYaw: Double
    var lastHeadYaw: Double
    var flyingSpeed: Float
    var sidewaysSpeed: Float
    var upwardSpeed: Float
    var forwardSpeed: Float
    var deSpawnCounter: Int
    val lookPitchSpeed: Double
    val lookMovingSpeed: Double
    val lookYawSpeed: Double
    val attackDistanceScalingFactor: Double
    val attributes: MutableMap<Int, Attribute>
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
    //var climbing: Vector3i?
    val random: Random get() = ThreadLocalRandom.current()

    fun addAttribute(attribute: Attribute) {
        attributes[attribute.id] = attribute
    }

    fun addAttribute(id: Int) {
        addAttribute(Attribute.getAttribute(id))
    }

    fun addAttributes(first: Int, vararg others: Int) {
        addAttribute(first)
        others.forEach(this::addAttribute)
    }
}
