package br.com.gamemods.mobai.math

import cn.nukkit.math.AxisAlignedBB
import cn.nukkit.math.SimpleAxisAlignedBB
import cn.nukkit.math.Vector3f

val AxisAlignedBB.isEmpty get() = minX >= maxX || minY >= maxY || minZ >= maxZ

fun AxisAlignedBB.copy(
    minX: Double = this.minX, minY: Double = this.minY, minZ: Double = this.minZ,
    maxX: Double = this.maxX, maxY: Double = this.maxY, maxZ: Double = this.maxZ
) = SimpleAxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ)

fun AxisAlignedBB.offsetCopy(offset: Vector3f) = getOffsetBoundingBox(offset.x, offset.y, offset.z)!!
fun AxisAlignedBB.offset(offset: Vector3f) = offset(offset.x, offset.y, offset.z)!!
