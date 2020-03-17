package br.com.gamemods.mobai.level

import cn.nukkit.block.Block
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.level.BlockPosition
import cn.nukkit.level.chunk.ChunkSection
import cn.nukkit.level.chunk.IChunk
import cn.nukkit.math.Vector3i
import cn.nukkit.utils.Identifier

class CachingChunk(val parent: IChunk): IChunk by parent {
    private val cachedBlocks = mutableMapOf<BlockPosition, Block>()
    private val cachedBlockEntities = mutableMapOf<Vector3i, BlockEntity>()
    private val cachedBlockLight = mutableMapOf<Vector3i, Byte>()
    private val cachedSkyLight = mutableMapOf<Vector3i, Byte>()
    private val cachedBiomes = mutableMapOf<Pair<Int, Int>, Int>()
    private val cachedHeightMap = mutableMapOf<Pair<Int, Int>, Int>()
    private val cachedSection = mutableMapOf<Int, ChunkSection>()
    override fun getBlockId(x: Int, y: Int, z: Int, layer: Int): Identifier {
        return getBlock(x, y, z, layer).id
    }

    override fun getBlockData(x: Int, y: Int, z: Int, layer: Int): Int {
        return getBlock(x, y, z, layer).damage
    }

    override fun getBlockEntity(x: Int, y: Int, z: Int): BlockEntity {
        return cachedBlockEntities.computeIfAbsent(Vector3i(x, y, z)) { parent.getBlockEntity(x, y, z) }
    }

    override fun getBiome(x: Int, z: Int): Int {
        return cachedBiomes.computeIfAbsent(x to z) { parent.getBiome(x, z) }
    }

    override fun getBlockLight(x: Int, y: Int, z: Int): Byte {
        return cachedBlockLight.computeIfAbsent(Vector3i(x, y, z)) { parent.getBlockLight(x, y, z) }
    }

    override fun getSkyLight(x: Int, y: Int, z: Int): Byte {
        return cachedSkyLight.computeIfAbsent(Vector3i(x, y, z)) { parent.getSkyLight(x, y, z) }
    }

    override fun getSection(y: Int): ChunkSection? {
        return cachedSection.getOrPut(y) { parent.getSection(y) ?: return null }
    }

    override fun getHeightMap(x: Int, z: Int): Int {
        return cachedHeightMap.computeIfAbsent(x to z) { parent.getHeightMap(x, z) }
    }

    override fun getBlock(x: Int, y: Int, z: Int, layer: Int): Block {
        return cachedBlocks.computeIfAbsent(BlockPosition(x, y, z, null, layer)) { parent.getBlock(x, y, z, layer) }
    }
}
