package br.com.gamemods.mobai.ai.pathing

class TargetPathNode(node: PathNode): PathNode(node.x, node.y, node.z) {
    var nearestNodeDistance = Float.MAX_VALUE; private set
    lateinit var nearestNode: PathNode; private set
    var isReached = false; private set

    fun updateNearestNode(distance: Float, node: PathNode) {
        if (distance < nearestNodeDistance) {
            nearestNodeDistance = distance
            nearestNode = node
        }
    }

    fun markReached() {
        isReached = true
    }
}
