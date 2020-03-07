package br.com.gamemods.mobai.level

import br.com.gamemods.mobai.math.chunkIndex
import cn.nukkit.block.Block
import cn.nukkit.level.BlockPosition
import cn.nukkit.level.ChunkManager
import cn.nukkit.level.Level
import cn.nukkit.level.chunk.Chunk
import cn.nukkit.level.chunk.IChunk
import cn.nukkit.math.Vector3i
import cn.nukkit.utils.Identifier

class ChunkCache(val level: Level, from: Vector3i, to: Vector3i): ChunkManager {
    private val minX = from.x shr 4
    private val minZ = from.z shr 4
    private val chunks: Array<Array<Chunk?>> = Array((to.x shr 4) - minX + 1) { indexX ->
        Array((to.z shr 4) - minZ + 1) { indexZ ->
            level.getLoadedChunk(indexX + minX, indexZ + minZ).takeIf {
                it?.isGenerated == true
            }
        }
    }

    private val isEmpty = (minX..(to.x shr 4)).asSequence()
        .flatMap { chunkX ->
            (minZ..(to.z shr 4)).asSequence()
                .mapNotNull { chunkZ ->
                    chunks[chunkX - minX][chunkZ - minZ]
                }
        }
        .all { chunk ->
            chunk.isEmpty(from.y, to.y)
        }

    fun getChunk(blockPos: Vector3i) = getChunk(blockPos.x shr 4, blockPos.z shr 4)

    override fun getChunk(chunkX: Int, chunkZ: Int): IChunk {
        val indexX = chunkX - minX
        val indexZ = chunkZ - minZ
        return chunks.getOrNull(indexX)?.getOrNull(indexZ)
            ?: EmptyChunk(level, chunkX, chunkZ)
    }

    fun getBlockEntity(pos: Vector3i) = getChunk(pos).getBlockEntity(pos.chunkIndex())

    fun getBlock(pos: Vector3i) = getChunk(pos).getBlock(pos.chunkIndex())

    fun getBlock(pos: BlockPosition) = getChunk(pos).getBlock(pos.chunkIndex())

    override fun getBlockIdAt(x: Int, y: Int, z: Int, layer: Int): Identifier {
        val pos = Vector3i(x, y, z)
        return getChunk(pos).getBlockId(pos.chunkIndex(), layer)
    }

    override fun getBlockAt(x: Int, y: Int, z: Int, layer: Int) = getBlock(BlockPosition(x, y, z, level, layer))

    override fun setBlockRuntimeIdUnsafe(x: Int, y: Int, z: Int, layer: Int, runtimeId: Int) {
        throw UnsupportedOperationException()
    }

    override fun setBlockIdAt(x: Int, y: Int, z: Int, layer: Int, id: Identifier?) {
        throw UnsupportedOperationException()
    }

    override fun getBlockRuntimeIdUnsafe(x: Int, y: Int, z: Int, layer: Int): Int {
        val pos = Vector3i(x, y, z)
        return getChunk(pos).getBlockRuntimeIdUnsafe(pos.chunkIndex(), layer)
    }

    override fun getBlockDataAt(x: Int, y: Int, z: Int, layer: Int): Int {
        val pos = Vector3i(x, y, z)
        return getChunk(pos).getBlockData(pos.chunkIndex(), layer)
    }

    override fun setBlockAt(x: Int, y: Int, z: Int, layer: Int, block: Block?) {
        throw UnsupportedOperationException()
    }

    override fun getSeed(): Long {
        return level.seed
    }

    override fun setBlockDataAt(x: Int, y: Int, z: Int, layer: Int, data: Int) {
        throw UnsupportedOperationException()
    }
}
