package br.com.gamemods.mobai.math

import cn.nukkit.level.BlockPosition
import cn.nukkit.level.Level
import cn.nukkit.math.Vector3f
import cn.nukkit.math.Vector3i

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
