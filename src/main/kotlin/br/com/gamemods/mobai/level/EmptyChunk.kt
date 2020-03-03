package br.com.gamemods.mobai.level

import cn.nukkit.block.Block
import cn.nukkit.block.BlockIds
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.entity.Entity
import cn.nukkit.level.Level
import cn.nukkit.level.chunk.ChunkSection
import cn.nukkit.level.chunk.IChunk
import cn.nukkit.player.Player
import cn.nukkit.utils.Identifier

class EmptyChunk(private val level: Level, private val x: Int, private val z: Int): IChunk {
    override fun getOrCreateSection(y: Int): ChunkSection {
        throw UnsupportedOperationException()
    }

    override fun clear() {
    }

    override fun setBlockData(x: Int, y: Int, z: Int, layer: Int, data: Int) {
        throw UnsupportedOperationException()
    }

    override fun addEntity(entity: Entity) {
        throw UnsupportedOperationException()
    }

    override fun setGenerated(generated: Boolean) {
        throw UnsupportedOperationException()
    }

    override fun setBlockId(x: Int, y: Int, z: Int, layer: Int, id: Identifier?) {
        throw UnsupportedOperationException()
    }

    override fun setPopulated(populated: Boolean) {
        throw UnsupportedOperationException()
    }

    override fun setBlockRuntimeIdUnsafe(x: Int, y: Int, z: Int, layer: Int, runtimeId: Int) {
        throw UnsupportedOperationException()
    }

    override fun clearDirty(): Boolean {
        return false
    }

    override fun getHeightMapArray(): IntArray {
        return IntArray(256)
    }

    override fun getBlockId(x: Int, y: Int, z: Int, layer: Int): Identifier {
        return BlockIds.AIR
    }

    override fun getBlockData(x: Int, y: Int, z: Int, layer: Int): Int {
        return 0
    }

    override fun getBlockEntity(x: Int, y: Int, z: Int): BlockEntity? {
        return null
    }

    override fun getPlayers(): Collection<Player> {
        return emptySet()
    }

    override fun setHeightMap(x: Int, z: Int, value: Int) {
        throw UnsupportedOperationException()
    }

    override fun getLevel(): Level {
        return level
    }

    override fun getBiome(x: Int, z: Int): Int {
        return 0
    }

    override fun getBiomeArray(): ByteArray {
        return ByteArray(256)
    }

    override fun isPopulated(): Boolean {
        return false
    }

    override fun setDirty(dirty: Boolean) {
        throw UnsupportedOperationException()
    }

    override fun setBiome(x: Int, z: Int, biome: Int) {
        throw UnsupportedOperationException()
    }

    override fun getAndSetBlock(x: Int, y: Int, z: Int, layer: Int, block: Block?): Block {
        throw UnsupportedOperationException()
    }

    override fun getBlockLight(x: Int, y: Int, z: Int): Byte {
        return 0
    }

    override fun getSections(): Array<ChunkSection?> {
        return arrayOfNulls(256)
    }

    override fun getX(): Int {
        return x
    }

    override fun removeBlockEntity(blockEntity: BlockEntity?) {
        throw UnsupportedOperationException()
    }

    override fun getZ(): Int {
        return z
    }

    override fun getSection(y: Int): ChunkSection? {
        return null
    }

    override fun getEntities(): Collection<Entity> {
        return emptySet()
    }

    override fun getBlockRuntimeIdUnsafe(x: Int, y: Int, z: Int, layer: Int): Int {
        return 0
    }

    override fun addBlockEntity(blockEntity: BlockEntity?) {
        throw UnsupportedOperationException()
    }

    override fun setBlockLight(x: Int, y: Int, z: Int, level: Int) {
        throw UnsupportedOperationException()
    }

    override fun getHeightMap(x: Int, z: Int): Int {
        return 0
    }

    override fun getSkyLight(x: Int, y: Int, z: Int): Byte {
        return 0
    }

    override fun isDirty(): Boolean {
        return false
    }

    override fun setBlock(x: Int, y: Int, z: Int, layer: Int, block: Block?) {
        throw UnsupportedOperationException()
    }

    override fun isGenerated(): Boolean {
        return false
    }

    override fun removeEntity(entity: Entity?) {
        throw UnsupportedOperationException()
    }

    override fun getBlock(x: Int, y: Int, z: Int, layer: Int): Block {
        return Block.get(BlockIds.AIR, 0, level, x, y, z, 0)
    }

    override fun setSkyLight(x: Int, y: Int, z: Int, level: Int) {
        throw UnsupportedOperationException()
    }

    override fun getBlockEntities(): Collection<BlockEntity> {
        return emptySet()
    }
}
