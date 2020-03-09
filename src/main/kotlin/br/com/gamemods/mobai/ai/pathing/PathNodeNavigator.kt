package br.com.gamemods.mobai.ai.pathing

import br.com.gamemods.mobai.entity.smart.EntityAI
import br.com.gamemods.mobai.entity.smart.SmartEntity
import br.com.gamemods.mobai.level.ChunkCache
import cn.nukkit.entity.impl.BaseEntity
import cn.nukkit.math.Vector3i
import java.util.*
import kotlin.math.min

class PathNodeNavigator<E>(private val pathNodeMaker: PathNodeMaker<E>, private val range: Int) where E: SmartEntity, E: BaseEntity {
    private val successors = arrayOfNulls<PathNode>(32)
    private val minHeap = PathMinHeap()

    fun findPathToAny(
        chunkCache: ChunkCache,
        ai: EntityAI<E>,
        entity: E,
        positions: Set<Vector3i>,
        followRange: Float,
        distance: Int,
        rangeMultiplier: Float
    ): Path? {
        minHeap.clear()
        pathNodeMaker.init(chunkCache, ai, entity)
        val pathNode = pathNodeMaker.start
        val map = positions.associateBy { pathNodeMaker.createTargetNode(it.asVector3f()) }
        val path = findPathToAny(pathNode, map, followRange, distance, rangeMultiplier)
        pathNodeMaker.clear()
        return path
    }

    private fun findPathToAny(
        startNode: PathNode,
        positions: Map<TargetPathNode, Vector3i>,
        followRange: Float,
        distance: Int,
        rangeMultiplier: Float
    ): Path? {
        val nodes = positions.keys
        startNode.penalizedPathLength = 0.0f
        startNode.distanceToNearestTarget = calculateDistances(startNode, nodes)
        startNode.heapWeight = startNode.distanceToNearestTarget
        minHeap.clear()
        minHeap.push(startNode)
        var currentDistance = 0
        val range = (range * rangeMultiplier).toInt()
        while (!minHeap.isEmpty) {
            if (++currentDistance >= range) {
                break
            }

            val pathNode = minHeap.pop()
            pathNode.visited = true
            nodes.asSequence().filter {
                pathNode.manhattanDistance(it) <= distance.toFloat()
            }.forEach(TargetPathNode::markReached)
            if (nodes.any(TargetPathNode::isReached)) {
                break
            }
            if (pathNode.distance(startNode) < followRange) {
                val count = pathNodeMaker.successors(successors, pathNode)
                for (i in 0 until count) {
                    val currentNode = checkNotNull(successors[i])
                    val nodeDistance = pathNode.distance(currentNode).toFloat()
                    currentNode.pathLength = pathNode.pathLength + nodeDistance
                    val penalizedDistance = pathNode.penalizedPathLength + nodeDistance + currentNode.penalty
                    if (currentNode.pathLength < followRange
                        && (!currentNode.isInHeap || penalizedDistance < currentNode.penalizedPathLength)
                    ) {
                        currentNode.previous = pathNode
                        currentNode.penalizedPathLength = penalizedDistance
                        currentNode.distanceToNearestTarget = calculateDistances(currentNode, nodes) * 1.5f
                        if (currentNode.isInHeap) {
                            minHeap.setNodeWeight(
                                currentNode,
                                currentNode.penalizedPathLength + currentNode.distanceToNearestTarget
                            )
                        } else {
                            currentNode.heapWeight = currentNode.penalizedPathLength + currentNode.distanceToNearestTarget
                            minHeap.push(currentNode)
                        }
                    }
                }
            }
        }

        var targetNodeSeq = nodes.asSequence()
        val reachesTarget = nodes.any(TargetPathNode::isReached)
        if (reachesTarget) {
            targetNodeSeq = targetNodeSeq.filter(TargetPathNode::isReached)
        }
        var pathSeq = targetNodeSeq.mapNotNull {
            createPath(it.nearestNode, checkNotNull(positions[it]), reachesTarget)
        }
        pathSeq = if (reachesTarget) {
            pathSeq.sortedWith(Comparator.comparingInt<Path>(Path::size))
        } else {
            pathSeq.sortedWith(
                Comparator.comparingDouble<Path> { it.manhattanDistanceFromTarget.toDouble() }
                    .thenComparingInt(Path::size)
            )
        }

        return pathSeq.firstOrNull()
    }

    private fun calculateDistances(node: PathNode, targets: Set<TargetPathNode>): Float {
        var nearestDistance = Float.MAX_VALUE
        targets.forEach { targetPathNode ->
            val distance = node.distance(targetPathNode).toFloat()
            targetPathNode.updateNearestNode(distance, node)
            nearestDistance = min(distance, nearestDistance)
        }
        return nearestDistance
    }

    private fun createPath(endNode: PathNode, target: Vector3i, reachesTarget: Boolean): Path? {
        val list = mutableListOf<PathNode>()

        var currentNode = endNode
        while (true) {
            list += currentNode
            currentNode = currentNode.previous ?: break
        }
        list.reverse()
        return Path(list, target, reachesTarget)
    }
}
