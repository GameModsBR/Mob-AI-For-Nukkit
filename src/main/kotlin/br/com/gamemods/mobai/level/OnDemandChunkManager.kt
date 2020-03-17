package br.com.gamemods.mobai.level

import cn.nukkit.block.Block
import cn.nukkit.block.BlockIds
import cn.nukkit.level.ChunkManager
import cn.nukkit.level.Level
import cn.nukkit.level.chunk.Chunk
import cn.nukkit.level.chunk.IChunk
import cn.nukkit.utils.Identifier

class OnDemandChunkManager(val level: Level, vararg initialChunks: IChunk): ChunkManager {
    private val cache: MutableMap<Long, IChunk> = LinkedHashMap((initialChunks.size.takeIf { it > 0 } ?: 1) + 1)
    init {
        initialChunks.forEach(::plusAssign)
    }

    private fun validateChunk(chunk: IChunk) {
        check(chunk.level === level) { "The chunk ${chunk.x}x${chunk.z} is from a different level! (From: ${chunk.level.id}, Expected: ${level.id}" }
    }

    operator fun plusAssign(chunk: IChunk) {
        validateChunk(chunk)
        cache[Chunk.key(chunk.x, chunk.z)] = chunk
    }

    operator fun minusAssign(chunk: IChunk) {
        validateChunk(chunk)
        cache -= Chunk.key(chunk.x, chunk.z)
    }

    fun clear() {
        cache.clear()
    }

    override fun getBlockIdAt(x: Int, y: Int, z: Int, layer: Int): Identifier {
        if (y !in 0..255) {
            return BlockIds.AIR
        }
        return getChunkFromBlock(x, z).getBlockId(x.index, y, z.index, layer)
    }

    override fun getBlockAt(x: Int, y: Int, z: Int, layer: Int): Block {
        if (y !in 0..255) {
            return Block.get(BlockIds.AIR, 0, level, x, y, z, layer)
        }
        return getChunkFromBlock(x, z).getBlock(x.index, y, z.index, layer)
    }

    override fun setBlockRuntimeIdUnsafe(x: Int, y: Int, z: Int, layer: Int, runtimeId: Int) {
        level.setBlockRuntimeIdUnsafe(x, y, z, layer, runtimeId)
        cacheChunkFromBlock(x, z)
    }

    override fun setBlockIdAt(x: Int, y: Int, z: Int, layer: Int, id: Identifier?) {
        level.setBlockIdAt(x, y, z, layer, id)
        cacheChunkFromBlock(x, z)
    }

    override fun getBlockRuntimeIdUnsafe(x: Int, y: Int, z: Int, layer: Int): Int {
        if (y !in 0..255) {
            return 0
        }
        return getChunkFromBlock(x, z).getBlockRuntimeIdUnsafe(x.index, y, z.index, layer)
    }

    override fun getBlockDataAt(x: Int, y: Int, z: Int, layer: Int): Int {
        if (y !in 0..255) {
            return 0
        }
        return getChunkFromBlock(x, z).getBlockData(x.index, y, z.index, layer)
    }

    override fun setBlockAt(x: Int, y: Int, z: Int, layer: Int, block: Block?) {
        level.setBlockAt(x, y, z, layer, block)
        cacheChunkFromBlock(x, z)
    }

    override fun setBlockDataAt(x: Int, y: Int, z: Int, layer: Int, data: Int) {
        level.setBlockDataAt(x, y, z, layer, data)
        cacheChunkFromBlock(x, z)
    }

    private inline val Int.index get() = this and 0x0F

    override fun getChunk(chunkX: Int, chunkZ: Int) = getChunk(Chunk.key(chunkX, chunkZ))

    private fun cacheChunkFromBlock(x: Int, z: Int) {
        getChunkFromBlock(x, z)
    }

    private fun getChunkFromBlock(x: Int, z: Int) = getChunk(chunkKeyFromBlock(x, z))

    private fun chunkKeyFromBlock(x: Int, z: Int) = Chunk.key(x shr 4, z shr 4)

    private fun getChunk(chunkKey: Long): IChunk = cache.computeIfAbsent(chunkKey) { CachingChunk(level.getChunk(chunkKey)) }

    override fun getSeed() = level.seed
}
