package br.com.gamemods.mobai.math

import cn.nukkit.entity.Entity
import cn.nukkit.entity.impl.BaseEntity
import cn.nukkit.level.BlockPosition
import cn.nukkit.level.Level
import cn.nukkit.level.chunk.IChunk
import cn.nukkit.math.Vector3f
import cn.nukkit.math.Vector3i
import kotlin.math.sqrt

val ZERO_3F = Vector3f()
val ZERO_3I = Vector3i()

fun Vector3f.isWithinDistance(pos: Vector3f, distance: Double): Boolean {
    return distanceSquared(pos, true) < distance.square()
}

fun Vector3f.distanceSquared(pos: Vector3f, treatAsBlock: Boolean): Double {
    val offset = if (treatAsBlock) 0.5 else 0.0
    val x = this.x + offset - pos.x
    val y = this.y + offset - pos.y
    val z = this.z + offset - pos.z
    return x.square() + y.square() + z.square()
}

fun Vector3i.chunkIndex() = Vector3i(MobAiMath.horizontalChunkBlock(x), y, MobAiMath.horizontalChunkBlock(z))
fun BlockPosition.chunkIndex() = BlockPosition(MobAiMath.horizontalChunkBlock(x), y, MobAiMath.horizontalChunkBlock(z), level, layer)

operator fun Vector3i.component1() = x
operator fun Vector3i.component2() = y
operator fun Vector3i.component3() = z
operator fun BlockPosition.component4() = layer
operator fun BlockPosition.component5(): Level? = level

operator fun Vector3f.component1() = x
operator fun Vector3f.component2() = y
operator fun Vector3f.component3() = z

operator fun Vector3i.plus(other: Vector3i) = add(other)!!
operator fun Vector3i.minus(other: Vector3i) = subtract(other)!!
operator fun Vector3i.times(other: Vector3i) = clone().apply { x *= other.x; y *= other.y; z *= other.z }
operator fun Vector3i.div(other: Vector3i) = clone().apply { x /= other.x; y /= other.y; z /= other.z }

operator fun Vector3f.plus(other: Vector3f) = add(other)!!
operator fun Vector3f.minus(other: Vector3f) = subtract(other)!!
operator fun Vector3f.times(other: Vector3f) = clone().apply { x *= other.x; y *= other.y; z *= other.z }
operator fun Vector3f.div(other: Vector3f) = clone().apply { x /= other.x; y /= other.y; z /= other.z }

operator fun Vector3i.plus(other: Vector3f) = Vector3f(x + other.x, y + other.y, z + other.z)
operator fun Vector3i.minus(other: Vector3f) = Vector3f(x - other.x, y-+ other.y, z - other.z)
operator fun Vector3i.times(other: Vector3f) = Vector3f(x * other.x, y*+ other.y, z * other.z)
operator fun Vector3i.div(other: Vector3f) = Vector3f(x / other.x, y/+ other.y, z / other.z)

operator fun Vector3f.plus(other: Vector3i) = Vector3f(x + other.x, y + other.y, z + other.z)
operator fun Vector3f.minus(other: Vector3i) = Vector3f(x - other.x, y-+ other.y, z - other.z)
operator fun Vector3f.times(other: Vector3i) = Vector3f(x * other.x, y*+ other.y, z * other.z)
operator fun Vector3f.div(other: Vector3i) = Vector3f(x / other.x, y/+ other.y, z / other.z)

fun Vector3f.distanceSquared(entity: Entity) = distanceSquared(entity as? Vector3f ?: entity.position)
fun Vector3i.distanceSquared(entity: Entity) = distanceSquared(entity as? Vector3f ?: entity.position)
fun Vector3f.distance(entity: Entity) = distance(entity as? Vector3f ?: entity.position)
fun Vector3i.distance(entity: Entity) = distance(entity as? Vector3f ?: entity.position)

fun Entity.distanceSquared(other: Vector3i) = (this as? Vector3f ?: position).distanceSquared(other)
fun Entity.distanceSquared(other: Vector3f) = (this as? Vector3f ?: position).distanceSquared(other)
fun Entity.distanceSquared(other: Entity) = (this as? Vector3f ?: position).distanceSquared(other)
fun Entity.distanceSquared(other: BaseEntity) = (this as? Vector3f ?: position).distanceSquared(other)

fun BaseEntity.distanceSquared(other: Entity) = (this as Vector3f).distanceSquared(other)

fun Entity.distance(other: Vector3i) = (this as? Vector3f ?: position).distance(other)
fun Entity.distance(other: Vector3f) = (this as? Vector3f ?: position).distance(other)
fun Entity.distance(other: Entity) = (this as? Vector3f ?: position).distance(other)
fun Entity.distance(other: BaseEntity) = (this as? Vector3f ?: position).distance(other)

fun BaseEntity.distance(other: Entity) = (this as Vector3f).distance(other)

private fun distanceSquared(fromX: Int, fromZ: Int, toX: Int, toZ: Int): Int {
    return (fromX - toX).square() + (fromZ - toZ).square()
}

fun IChunk.distanceSquared(x: Int, z: Int) = distanceSquared(this.x, this.z, x, z)
fun IChunk.distanceSquared(chunk: IChunk) = distanceSquared(chunk.x, chunk.z)
fun IChunk.distanceSquared(blockPos: Vector3i) = distanceSquared(blockPos.chunkX, blockPos.chunkZ)
fun IChunk.distanceSquared(entityPos: Vector3f) = distanceSquared(entityPos.chunkX, entityPos.chunkZ)

fun IChunk.distance(x: Int, z: Int) = sqrt(distanceSquared(x, z).toDouble())
fun IChunk.distance(chunk: IChunk) = distance(chunk.x, chunk.z)
fun IChunk.distance(blockPos: Vector3i) = distance(blockPos.chunkX, blockPos.chunkZ)
fun IChunk.distance(entityPos: Vector3f) = distance(entityPos.chunkX, entityPos.chunkZ)

fun Vector3f.distanceSquared(chunkX: Int, chunkZ: Int) = distanceSquared(this.chunkX, this.chunkZ, chunkX, chunkZ)
fun Vector3f.distanceSquared(chunk: IChunk) = chunk.distanceSquared(this)
fun Vector3f.distance(chunkX: Int, chunkZ: Int) = sqrt(distanceSquared(chunkX, chunkZ).toDouble())
fun Vector3f.distance(chunk: IChunk) = chunk.distance(this)

fun Vector3i.distanceSquared(chunkX: Int, chunkZ: Int) = distanceSquared(this.chunkX, this.chunkZ, chunkX, chunkZ)
fun Vector3i.distanceSquared(chunk: IChunk) = chunk.distanceSquared(this)
fun Vector3i.distance(chunkX: Int, chunkZ: Int) = sqrt(distanceSquared(chunkX, chunkZ).toDouble())
fun Vector3i.distance(chunk: IChunk) = chunk.distance(this)
