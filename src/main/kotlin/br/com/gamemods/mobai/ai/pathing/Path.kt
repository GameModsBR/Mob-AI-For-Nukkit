package br.com.gamemods.mobai.ai.pathing

import cn.nukkit.entity.Entity
import cn.nukkit.math.Vector3f
import cn.nukkit.math.Vector3i

class Path(val nodes: MutableList<PathNode>, val target: Vector3i?, val reachesTarget: Boolean) {
    private var unkA = emptyArray<PathNode>()
    private var unkB = emptyArray<PathNode>()
    private var currentNodeIndex = 0
    private val manhattanDistanceFromTarget = nodes.lastOrNull()?.manhattanDistance(requireNotNull(target) {
        "Non-Empty paths must have a target"
    } ) ?: Float.MIN_VALUE

    val isFinished get() = currentNodeIndex >= nodes.size
    val end get() = nodes.lastOrNull()
    val length get() = nodes.size
    val currentPosition = nodes[currentNodeIndex].asVector3f()

    fun next() = ++currentNodeIndex
    operator fun get(index: Int) = nodes[index]
    operator fun set(index: Int, node: PathNode) = nodes.set(index, node)

    fun setLength(length: Int) {
        if (nodes.size > length) {
            nodes.subList(length, nodes.size).clear()
        }
    }

    fun nodePosition(entity: Entity, index: Int = currentNodeIndex): Vector3f {
        val node = nodes[index]
        val width = (entity.width + 1F).toInt() * 0.5
        return Vector3f(
            node.x + width,
            node.y.toDouble(),
            node.z + width
        )
    }

    fun equalsPath(path: Path?): Boolean {
        if (path?.nodes?.size != this.nodes.size) {
            return false
        }

        nodes.forEachIndexed { i, pathNode ->
            val pathNode2 = path.nodes[i]
            if (pathNode.x != pathNode2.x || pathNode.y != pathNode2.y || pathNode.z != pathNode2.z) {
                return false
            }
        }

        return true
    }
}
