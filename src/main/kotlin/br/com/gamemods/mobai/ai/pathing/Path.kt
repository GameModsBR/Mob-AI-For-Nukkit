package br.com.gamemods.mobai.ai.pathing

import cn.nukkit.entity.Entity
import cn.nukkit.math.Vector3f
import cn.nukkit.math.Vector3i

class Path(
    nodes: MutableList<PathNode>,
    val target: Vector3i?,
    val reachesTarget: Boolean
): MutableList<PathNode> by nodes {
    var currentNodeIndex = 0
    val manhattanDistanceFromTarget = nodes.lastOrNull()?.manhattanDistance(requireNotNull(target) {
        "Non-Empty paths must have a target"
    } ) ?: Float.MAX_VALUE

    val isFinished get() = currentNodeIndex >= size
    val end get() = lastOrNull()
    val currentPosition get() = this[currentNodeIndex].asVector3f()!!

    fun next() = ++currentNodeIndex

    fun setLength(length: Int) {
        if (size > length) {
            subList(length, size).clear()
        }
    }

    fun nodePosition(entity: Entity, index: Int = currentNodeIndex): Vector3f {
        val node = this[index]
        val width = (entity.width + 1F).toInt() * 0.5
        return Vector3f(
            node.x + width,
            node.y.toDouble(),
            node.z + width
        )
    }

    fun equalsPath(path: Path?): Boolean {
        if (path?.size != this.size) {
            return false
        }

        forEachIndexed { i, pathNode ->
            val pathNode2 = path[i]
            if (pathNode.x != pathNode2.x || pathNode.y != pathNode2.y || pathNode.z != pathNode2.z) {
                return false
            }
        }

        return true
    }

    override fun toString(): String {
        return "Path(length=$size)"
    }
}
