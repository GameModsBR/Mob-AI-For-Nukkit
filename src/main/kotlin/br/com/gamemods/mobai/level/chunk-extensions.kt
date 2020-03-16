package br.com.gamemods.mobai.level

import br.com.gamemods.mobai.math.clamp
import cn.nukkit.block.Block
import cn.nukkit.block.BlockIds
import cn.nukkit.block.BlockWater
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.level.BlockPosition
import cn.nukkit.level.biome.Biome
import cn.nukkit.level.chunk.IChunk
import cn.nukkit.math.Vector3i
import cn.nukkit.utils.Identifier
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.math.max

fun IChunk.isEmpty(minY: Int, maxY: Int): Boolean {
    for (secY in (minY.coerceAtLeast(0))..(maxY.coerceAtMost(255)) step 16) {
        if (getSection(secY shr 4)?.isEmpty != false) {
            return false
        }
    }
    return true
}

private fun Vector3i.onlyValidHeights() = takeIf { y in 0..255 }

private inline fun <R> Vector3i.ifHasValidHeight(action: () -> R): R? {
    contract { callsInPlace(action, InvocationKind.AT_MOST_ONCE) }
    return onlyValidHeights()?.let { action() }
}

fun IChunk.getBlockEntity(pos: Vector3i): BlockEntity? = pos.ifHasValidHeight { getBlockEntity(pos.x, pos.y, pos.z) }
fun IChunk.getBlockLight(pos: Vector3i): Int {
    val y = pos.y.clamp(0, 255)
    return getSection(y shr 4)?.getBlockLight(pos.x, y and 0xF, pos.z)?.toInt() ?: 0
}
fun IChunk.getSkyLight(pos: Vector3i): Int {
    val y = pos.y.clamp(0, 255)
    return getSection(y shr 4)?.getSkyLight(pos.x, y and 0xF, pos.z)?.toInt() ?: 0
}

fun IChunk.getBlockId(pos: Vector3i, layer: Int = 0): Identifier = pos.ifHasValidHeight { getBlockId(pos.x, pos.y, pos.z, layer) } ?: BlockIds.AIR
fun IChunk.getBlock(pos: Vector3i, layer: Int = 0): Block = pos.ifHasValidHeight { getBlock(pos.x, pos.y, pos.z, layer) } ?: Block.get(BlockIds.AIR, 0, level, (x shl 4) + pos.x, pos.y, (z shl 4) + pos.z, layer)
fun IChunk.getBlockData(pos: Vector3i, layer: Int = 0) = pos.ifHasValidHeight { getBlockData(pos.x, pos.y, pos.z, layer) } ?: 0
fun IChunk.getBlockRuntimeIdUnsafe(pos: Vector3i, layer: Int = 0) = pos.ifHasValidHeight { getBlockRuntimeIdUnsafe(pos.x, pos.y, pos.z, layer) } ?: 0
fun IChunk.getBlockId(pos: BlockPosition) = getBlockId(pos, pos.layer)

fun IChunk.getBlock(pos: BlockPosition) = getBlock(pos, pos.layer)
fun IChunk.getBlockData(pos: BlockPosition) = getBlockData(pos, pos.layer)
fun IChunk.getBlockRuntimeIdUnsafe(pos: BlockPosition) = getBlockRuntimeIdUnsafe(pos, pos.layer)

fun IChunk.getBiomeId(pos: Vector3i) = getBiome(pos.x, pos.z)
fun IChunk.getBiome(pos: Vector3i): Biome = Biome.getBiome(getBiomeId(pos))

operator fun IChunk.get(pos: Vector3i, layer: Int = 0) = getBlock(pos, layer)
operator fun IChunk.get(pos: BlockPosition) = getBlock(pos)

fun IChunk.isFlooded(pos: Vector3i): Boolean {
    return when (getBlockId(pos, 0)) {
        BlockIds.WATER, BlockIds.FLOWING_WATER -> true
        BlockIds.AIR -> false
        else -> when (getBlockId(pos, 1)) {
            BlockIds.WATER, BlockIds.FLOWING_WATER -> true
            else -> false
        }
    }
}


fun IChunk.getWaterDamage(pos: Vector3i): Int {
    for(layer in 0..1) {
        val block = getBlock(pos, layer)
        if (block.id == BlockIds.AIR) {
            return -1
        }
        if (block is BlockWater) {
            return block.damage
        }
    }
    return -1
}

fun IChunk.getLight(pos: Vector3i, ambientDarkness: Int): Int {
    val skyLight = getSkyLight(pos) - ambientDarkness
    val blockLight = getBlockLight(pos)
    return max(blockLight, skyLight).clamp(0, 15)
}
