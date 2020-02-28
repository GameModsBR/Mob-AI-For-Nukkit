package br.com.gamemods.mobai.ai.pathing

import br.com.gamemods.mobai.math.MobAiMath.square
import cn.nukkit.math.Vector3f
import cn.nukkit.math.Vector3i
import kotlin.math.abs

class PathNode(x: Int, y: Int, z: Int) : Vector3i(x, y, z) {
    //private val hashCode = hash(x, y, z)
    var heapIndex = -1
    var penalizedPathLength = 0F
    var distanceToNearestTarget = 0F
    var heapWeight = 0F
    var previous: PathNode? = null
    var visited = false
    var pathLength = 0F
    var penalty = 0F
    var type = PathNodeType.BLOCKED

    val isInHeap get() = heapIndex >= 0

    fun copy(position: Vector3f,
             heapIndex: Int = this.heapIndex,
             penalizedPathLength: Float = this.penalizedPathLength,
             distanceToNearestTarget: Float = this.distanceToNearestTarget,
             heapWeight: Float = this.heapWeight,
             previous: PathNode? = this.previous,
             visited: Boolean = this.visited,
             pathLength: Float = this.pathLength,
             penalty: Float = this.penalty,
             type: PathNodeType = this.type
    ) = copy(position.floorX, position.floorY, position.floorZ, heapIndex, penalizedPathLength, distanceToNearestTarget, heapWeight, previous, visited, pathLength, penalty, type)

    fun copy(position: Vector3i,
             heapIndex: Int = this.heapIndex,
             penalizedPathLength: Float = this.penalizedPathLength,
             distanceToNearestTarget: Float = this.distanceToNearestTarget,
             heapWeight: Float = this.heapWeight,
             previous: PathNode? = this.previous,
             visited: Boolean = this.visited,
             pathLength: Float = this.pathLength,
             penalty: Float = this.penalty,
             type: PathNodeType = this.type
    ) = copy(position.x, position.y, position.z, heapIndex, penalizedPathLength, distanceToNearestTarget, heapWeight, previous, visited, pathLength, penalty, type)

    fun copy(x: Int = this.x, y: Int = this.y, z: Int = this.z,
             heapIndex: Int = this.heapIndex,
             penalizedPathLength: Float = this.penalizedPathLength,
             distanceToNearestTarget: Float = this.distanceToNearestTarget,
             heapWeight: Float = this.heapWeight,
             previous: PathNode? = this.previous,
             visited: Boolean = this.visited,
             pathLength: Float = this.pathLength,
             penalty: Float = this.penalty,
             type: PathNodeType = this.type
    ): PathNode {
        return PathNode(x, y, z).also {
            it.heapIndex = heapIndex
            it.penalizedPathLength = penalizedPathLength
            it.distanceToNearestTarget = distanceToNearestTarget
            it.heapWeight = heapWeight
            it.previous = previous
            it.visited = visited
            it.pathLength = pathLength
            it.penalty = penalty
            it.type = type
        }
    }

    override fun distanceSquared(x: Double, y: Double, z: Double): Double {
        return square(x - this.x) + square(y - this.y) + square(z - this.z)
    }

    fun manhattanDistance(pos: Vector3i): Float {
        val x = abs(pos.x - x).toFloat()
        val y = abs(pos.y - y).toFloat()
        val z = abs(pos.z - z).toFloat()
        return x + y + z
    }

    fun manhattanDistance(pos: Vector3f): Double {
        return abs(pos.x - x) + abs(pos.y - y) + abs(pos.z - z)
    }

    override fun toString(): String {
        return "PathNode(x=$x, y=$y, z=$z)"
    }


    /*companion object {
        private const val shortMax = Short.MAX_VALUE.toInt()
        fun hash(x: Int, y: Int, z: Int): Int {
            return (y and 255) or (x and shortMax) shl 8 or (z and shortMax) shl 24 or
                    (if (x < 0) Int.MIN_VALUE else 0) or
                    (if (z < 0) shortMax + 1 else 0)
        }
    }*/
}
