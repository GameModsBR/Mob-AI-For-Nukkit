package br.com.gamemods.mobai.level

import br.com.gamemods.mobai.ai.filter.TargetFilter
import br.com.gamemods.mobai.math.chunkIndex
import br.com.gamemods.mobai.math.intCeil
import br.com.gamemods.mobai.math.intFloor
import cn.nukkit.block.Block
import cn.nukkit.block.BlockIds.*
import cn.nukkit.block.BlockLiquid
import cn.nukkit.entity.Entity
import cn.nukkit.entity.impl.BaseEntity
import cn.nukkit.level.BlockPosition
import cn.nukkit.level.ChunkManager
import cn.nukkit.level.Level
import cn.nukkit.level.chunk.Chunk
import cn.nukkit.level.chunk.IChunk
import cn.nukkit.level.generator.noise.nukkit.f.NoiseF
import cn.nukkit.math.AxisAlignedBB
import cn.nukkit.math.Vector3f
import cn.nukkit.math.Vector3i
import cn.nukkit.player.Player
import kotlin.reflect.KClass

val Level.height: Int get() = if (dimension == Level.DIMENSION_NETHER) 127 else 255

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
    return when (chunk.getBlockId(chunkIndex, 0)) {
        WATER, FLOWING_WATER -> true
        AIR -> false
        else -> when (chunk.getBlockId(chunkIndex, 1)) {
            WATER, FLOWING_WATER -> true
            else -> false
        }
    }
}

fun Level.isSkyVisible(pos: Vector3f): Boolean {
    return canBlockSeeSky(pos.asVector3i())
}

fun Level.getLoadedChunk(pos: Vector3i): Chunk? = getLoadedChunk(pos.x shr 4, pos.z shr 4)

private fun brightnessDelta(dimension: Int) = when (dimension) {
    Level.DIMENSION_NETHER -> 0.1F
    else -> 0F
}

private val lightLevelToBrightness = Array(3) { dimension ->
    FloatArray(16) { lightLevel ->
        val scale = lightLevel / 15f
        val brightness = scale / (4f - (3f * scale))
        NoiseF.lerp(brightnessDelta(dimension), brightness, 1F)
    }
}

fun Level.getBrightness(pos: Vector3i): Float {
    return lightLevelToBrightness.getOrNull(dimension)?.getOrNull(getFullLightFixed(pos)) ?: 0F
}

//TODO Remove after https://github.com/NukkitX/Nukkit/pull/1235 gets merged
@Deprecated("Temporary workaround")
fun Level.getFullLightFixed(pos: Vector3i): Int {
    val chunk: Chunk = this.getChunk(pos.chunkX, pos.chunkZ)
    var level = chunk.getSection(pos.y shr 4)?.getSkyLight(pos.x and 0x0f, pos.y and 0x0f, pos.z and 0x0f)?.toInt() ?: 0
    level -= this.skyLightSubtracted.toInt()
    if (level < 15) {
        level = chunk.getBlockLight(pos.x and 0x0f, pos.y and 0xff, pos.z and 0x0f).toInt().coerceAtLeast(level)
    }
    return level
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

fun ChunkManager.getChunk(pos: Vector3i): IChunk? = getChunk(pos.x shr 4, pos.z shr 4)
fun Level.getChunk(pos: Vector3i) = getChunk(pos.x shr 4, pos.z shr 4)

operator fun ChunkManager.get(pos: Vector3f, layer: Int = 0) = get(pos.asVector3i(), layer)
operator fun ChunkManager.get(pos: Vector3i, layer: Int = 0) = get(pos.x, pos.y, pos.z, layer)
operator fun ChunkManager.get(pos: BlockPosition) = get(pos.x, pos.y, pos.z, pos.layer)

operator fun ChunkManager.get(x: Int, y: Int, z: Int, layer: Int = 0): Block = if (this is Level) get(x, y, z, layer) else getBlockAt(x, y, z, layer)
operator fun Level.get(x: Int, y: Int, z: Int, layer: Int = 0) = getBlock(x, y, z, layer)
