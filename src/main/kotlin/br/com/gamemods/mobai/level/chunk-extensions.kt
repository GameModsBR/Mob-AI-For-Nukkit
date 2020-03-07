package br.com.gamemods.mobai.level

import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.level.BlockPosition
import cn.nukkit.level.chunk.IChunk
import cn.nukkit.math.Vector3i

fun IChunk.isEmpty(minY: Int, maxY: Int): Boolean {
    for (secY in (minY.coerceAtLeast(0))..(maxY.coerceAtMost(255)) step 16) {
        if (getSection(secY shr 4)?.isEmpty != false) {
            return false
        }
    }
    return true
}

fun IChunk.getBlockEntity(pos: Vector3i): BlockEntity? = getBlockEntity(pos.x, pos.y, pos.z)
fun IChunk.getBlockLight(pos: Vector3i) = getBlockLight(pos.x, pos.y, pos.z)

fun IChunk.getBlockId(pos: Vector3i, layer: Int = 0) = getBlockId(pos.x, pos.y, pos.z, layer)
fun IChunk.getBlock(pos: Vector3i, layer: Int = 0) = getBlock(pos.x, pos.y, pos.z, layer)
fun IChunk.getBlockData(pos: Vector3i, layer: Int = 0) = getBlockData(pos.x, pos.y, pos.z, layer)
fun IChunk.getBlockRuntimeIdUnsafe(pos: Vector3i, layer: Int = 0) = getBlockRuntimeIdUnsafe(pos.x, pos.y, pos.z, layer)

fun IChunk.getBlockId(pos: BlockPosition) = getBlockId(pos, pos.layer)
fun IChunk.getBlock(pos: BlockPosition) = getBlock(pos, pos.layer)
fun IChunk.getBlockData(pos: BlockPosition) = getBlockData(pos, pos.layer)
fun IChunk.getBlockRuntimeIdUnsafe(pos: BlockPosition) = getBlockRuntimeIdUnsafe(pos, pos.layer)
