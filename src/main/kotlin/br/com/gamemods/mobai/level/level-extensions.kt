package br.com.gamemods.mobai.level

import br.com.gamemods.mobai.ai.filter.TargetFilter
import br.com.gamemods.mobai.delegators.custom.customField
import br.com.gamemods.mobai.level.spawning.LevelSettings
import br.com.gamemods.mobai.math.chunkIndex
import br.com.gamemods.mobai.math.clamp
import br.com.gamemods.mobai.math.intCeil
import br.com.gamemods.mobai.math.intFloor
import cn.nukkit.block.Block
import cn.nukkit.block.BlockLiquid
import cn.nukkit.entity.Entity
import cn.nukkit.entity.impl.BaseEntity
import cn.nukkit.level.BlockPosition
import cn.nukkit.level.ChunkManager
import cn.nukkit.level.Level
import cn.nukkit.level.chunk.Chunk
import cn.nukkit.level.chunk.IChunk
import cn.nukkit.level.gamerule.GameRules
import cn.nukkit.level.generator.noise.nukkit.f.NoiseF
import cn.nukkit.math.*
import cn.nukkit.player.Player
import kotlin.math.cos
import kotlin.reflect.KClass
import kotlin.reflect.full.cast
import kotlin.reflect.full.safeCast

val Level.height: Int get() = if (dimensionType.isNether) 127 else 255
val Level.doMobLoot: Boolean get() = gameRules[GameRules.DO_MOB_LOOT]
val Level.effectiveSettings get() = LevelSettings.getEffective(this)
val Level.difficulty get() = effectiveSettings.difficulty.takeIf { it >= 0 } ?: server.difficulty
val Level.spawnAnimals get() = effectiveSettings.spawnAnimals ?: true
val Level.spawnMonsters get() = effectiveSettings.spawnMonsters ?: true
inline val Level.dimensionType get() = Dimension(dimension)

fun Level.findClosestPlayer(filter: TargetFilter, cause: Entity): Player? {
    return findClosestPlayer(filter, cause, cause.position)
}

fun Level.findClosestPlayer(filter: TargetFilter, cause: Entity?, position: Vector3f): Player? {
    return findClosestEntity(players.values, filter, cause, position)
}

fun Level.findClosestEntity(
    typeFilter: KClass<out Entity>?,
    targetFilter: TargetFilter,
    cause: Entity?,
    position: Vector3f,
    bb: AxisAlignedBB
): Entity? {
    return findClosestEntity(getCollidingEntities(bb).run {
        if (typeFilter != null) {
            filter { typeFilter.java.isInstance(it) }
        } else {
            this
        }
    }, targetFilter, cause, position)
}

fun Level.findPlayers(near: Vector3f, maxDistance: Double = -1.0, ignoreCreative: Boolean = false): Sequence<Pair<Player, Double>> {
    return players.values.asSequence()
        .run {
            if (ignoreCreative) {
                filterNot { it.isSpectator || it.isCreative }
            } else {
                filterNot { it.isSpectator }
            }
        }
        .map { it to it.distanceSquared(near) }
        .run {
            if (maxDistance >= 0) {
                filter { (_, distance) ->
                    distance <= maxDistance
                }
            } else {
                this
            }
        }
}

fun Level.findPlayers(near: Vector2f, maxDistance: Double = -1.0, ignoreCreative: Boolean = false): Sequence<Pair<Player, Double>> {
    return players.values.asSequence()
        .run {
            if (ignoreCreative) {
                filterNot { it.isSpectator || it.isCreative }
            } else {
                filterNot { it.isSpectator }
            }
        }
        .map { it to Vector2f(it.x, it.z).distanceSquared(near) }
        .run {
            if (maxDistance >= 0) {
                filter { (_, distance) ->
                    distance <= maxDistance
                }
            } else {
                this
            }
        }
}

inline fun <reified E: Entity> Level.findEntities(
    bb: AxisAlignedBB,
    collisionCheck: Entity? = null,
    noinline filter: ((E) -> Boolean)? = null
): Collection<E> = findEntities(E::class, bb, collisionCheck, filter)

fun <E: Entity> Level.findEntities(
    filterClass: KClass<E>,
    bb: AxisAlignedBB,
    collisionCheck: Entity? = null,
    filter: ((E) -> Boolean)? = null
): Collection<E> {
    return getCollidingEntities(bb, collisionCheck)
        .asSequence()
        .run {
            if (filterClass != Entity::class) {
                mapNotNull { filterClass.safeCast(it) }
            } else {
                map { filterClass.cast(it) }
            }
        }
        .run {
            filter?.let { filter(it) } ?: this
        }
        .toList()
}

fun <T: Entity> Sequence<Pair<T, Double>>.closest() = maxBy { it.second }

fun <T: Entity> Collection<T>.distance(pos: Vector3f) = asSequence()
    .map { it to (it as? Vector3f ?: it.position).distanceSquared(pos) }

fun <T: Entity> Collection<T>.closestTo(pos: Vector3f): Pair<T, Double>? {
    return distance(pos).closest()
}

private fun <T : Entity> findClosestEntity(
    entityList: Collection<T>,
    targetPredicate: TargetFilter,
    entity: Entity?,
    position: Vector3f
): T? {
    var d = -1.0
    var livingEntity: T? = null
    val var13: Iterator<T> = entityList.iterator()
    while (true) {
        var livingEntity2: T
        var e: Double
        do {
            do {
                if (!var13.hasNext()) {
                    return livingEntity
                }
                livingEntity2 = var13.next()
            } while (!targetPredicate.test(entity, livingEntity2))
            e = livingEntity2.position.distanceSquared(position)
        } while (d != -1.0 && e >= d)
        d = e
        livingEntity = livingEntity2
    }
}

fun Level.isFlooded(pos: Vector3i): Boolean {
    val chunk = getLoadedChunk(pos) ?: return false
    val chunkIndex = pos.chunkIndex()
    return chunk.isFlooded(chunkIndex)
}

fun Level.isSkyVisible(pos: Vector3f): Boolean {
    return canBlockSeeSky(pos.asVector3i())
}

fun Level.getLoadedChunk(pos: Vector3i): Chunk? = getLoadedChunk(pos.chunkX, pos.chunkZ)

private fun brightnessDelta(dimension: Dimension) = if (dimension.isNether) 0.1F else 0F

private val lightLevelToBrightness = Array(Dimension.MAX.id + 1) { dimension ->
    FloatArray(16) { lightLevel ->
        val scale = lightLevel / 15f
        val brightness = scale / (4f - (3f * scale))
        NoiseF.lerp(brightnessDelta(Dimension(dimension)), brightness, 1F)
    }
}

fun Level.getBrightness(pos: Vector3i): Float {
    return lightLevelToBrightness.getOrNull(dimension)?.getOrNull(getLight(pos)) ?: 0F
}

fun Level.getLight(pos: Vector3i, ambientDarkness: Int = skyLightSubtractedFixed.intCeil()): Int {
    return (this as ChunkManager).getLight(pos, ambientDarkness)
}

fun ChunkManager.getLight(pos: Vector3i, level: Level) = getLight(pos, level.skyLightSubtractedFixed.intCeil())

fun ChunkManager.getLight(pos: Vector3i, ambientDarkness: Int): Int {
    val chunk = getChunk(pos.chunkX, pos.chunkZ) ?: return 0
    val chunkIndex = pos.chunkIndex()
    return chunk.getLight(chunkIndex, ambientDarkness)
}

fun ChunkManager.getBlockSkyLightAt(x: Int, y: Int, z: Int): Int {
    val chunk = getChunk(x shr 4, z shr 4) ?: return 0
    val clampedY = y.clamp(0, 255)
    return chunk.getSection(clampedY shr 4)?.getSkyLight(x and 0xF, clampedY and 0xF, z and 0xF)?.toInt() ?: 0
}

fun ChunkManager.getBlockSkyLightAt(pos: Vector3i): Int {
    val chunk = getChunk(pos.chunkX, pos.chunkZ) ?: return 0
    return chunk.getSkyLight(pos.chunkIndex())
}

fun Level.hasCollision(entity: Entity?, bb: AxisAlignedBB, entities: Boolean, fluids: Boolean): Boolean {
    if (!fluids) {
        return hasCollision(entity as? BaseEntity, bb, entities)
    }

    val minX = bb.minX.intFloor()
    val minY = bb.minY.intFloor()
    val minZ = bb.minZ.intFloor()
    val maxX = bb.maxX.intCeil()
    val maxY = bb.maxY.intCeil()
    val maxZ = bb.maxZ.intCeil()

    val pos = Vector3f()
    for (z in minZ..maxZ) {
        for (x in minX..maxX) {
            for (y in minY..maxY) {
                val block = getLoadedBlock(pos.setComponents(x.toDouble(), y.toDouble(), z.toDouble()))
                // Shouldn't walk into unloaded chunks.
                if (block == null || block is BlockLiquid || block.isWaterlogged
                    || !block.canPassThrough() && block.collidesWithBB(bb)) {
                    return true
                }
            }
        }
    }

    if (entities) {
        return getCollidingEntities(bb.grow(0.25, 0.25, 0.25), entity).isNotEmpty()
    }

    return false
}

fun ChunkManager.getBlockIdAt(pos: Vector3f, layer: Int = 0) = getBlockIdAt(pos.asVector3i(), layer)
fun ChunkManager.getBlockAt(pos: Vector3f, layer: Int = 0) = get(pos.asVector3i(), layer)
fun ChunkManager.getBlockDataAt(pos: Vector3f, layer: Int = 0) = getBlockDataAt(pos.asVector3i(), layer)

fun ChunkManager.getBlockIdAt(pos: Vector3i, layer: Int = 0) = getBlockIdAt(pos.x, pos.y, pos.z, layer)!!
fun ChunkManager.getBlockAt(pos: Vector3i, layer: Int = 0) = get(pos.x, pos.y, pos.z, layer)
fun ChunkManager.getBlockDataAt(pos: Vector3i, layer: Int = 0) = getBlockDataAt(pos.x, pos.y, pos.z, layer)

fun ChunkManager.getBlockIdAt(pos: BlockPosition) = getBlockIdAt(pos.x, pos.y, pos.z, pos.layer)!!
fun ChunkManager.getBlockAt(pos: BlockPosition) = get(pos.x, pos.y, pos.z, pos.layer)
fun ChunkManager.getBlockDataAt(pos: BlockPosition) = getBlockDataAt(pos.x, pos.y, pos.z, pos.layer)

fun ChunkManager.getChunk(pos: Vector3i): IChunk? = getChunk(pos.chunkX, pos.chunkZ)
fun Level.getChunk(pos: Vector3i) = getChunk(pos.chunkX, pos.chunkZ)

operator fun ChunkManager.get(pos: Vector3f, layer: Int = 0) = get(pos.asVector3i(), layer)
operator fun ChunkManager.get(pos: Vector3i, layer: Int = 0) = get(pos.x, pos.y, pos.z, layer)
operator fun ChunkManager.get(pos: BlockPosition) = get(pos.x, pos.y, pos.z, pos.layer)

operator fun ChunkManager.get(x: Int, y: Int, z: Int, layer: Int = 0): Block = if (this is Level) get(x, y, z, layer) else getBlockAt(x, y, z, layer)
operator fun Level.get(x: Int, y: Int, z: Int, layer: Int = 0) = getBlock(x, y, z, layer)

fun ChunkManager.getWaterDamage(blockPos: Vector3i): Int {
    val chunk = getChunk(blockPos).takeIf { it !is EmptyChunk } ?: return -1
    val chunkIndex = blockPos.chunkIndex()
    return chunk.getWaterDamage(chunkIndex)
}

fun Level.calculateCelestialAngleFixed(time: Int): Float {
    val i = (time % 24000L).toInt()
    var angle: Float = (i.toFloat() + 1) / 24000.0f - 0.25f

    if (angle < 0.0f) {
        ++angle
    }

    if (angle > 1.0f) {
        --angle
    }

    val f1 =
        1.0f - ((cos(angle.toDouble() * Math.PI) + 1.0) / 2.0).toFloat()
    angle += (f1 - angle) / 3.0f
    return angle
}

var Level.skyLightSubtractedFixed by customField(0F)
val Level.isDay: Boolean get() {
    val skyLightSubtractedFixed = skyLightSubtractedFixed
    return skyLightSubtractedFixed < 4
}
inline val Level.isNight get() = !isDay
fun Level.calculateSkylightSubtractedFixed(): Int {
    val angle = calculateCelestialAngleFixed(time)
    var light = 1.0F - (MathHelper.cos(angle * (Math.PI.toFloat() * 2F)) * 2.0F + 0.5F)
    light = light.clamp(0.0F, 1.0F)
    light = 1.0F - light
    light = (light.toDouble() * (1.0 - ((if(isRaining) 1 else 0) * 5.0f).toDouble() / 16.0)).toFloat()
    light = (light.toDouble() * (1.0 - ((if(isThundering) 1 else 0) * 5.0f).toDouble() / 16.0)).toFloat()
    light = 1.0F - light
    return (light * 11.0F).toInt()
}
