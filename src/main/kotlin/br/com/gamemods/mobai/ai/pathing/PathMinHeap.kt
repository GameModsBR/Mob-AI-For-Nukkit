package br.com.gamemods.mobai.ai.pathing

class PathMinHeap {
    private var pathNodes = arrayOfNulls<PathNode>(128)
    private var count = 0

    fun push(node: PathNode): PathNode {
        check(node.heapIndex == -1)

        if (count == pathNodes.size) {
            this.pathNodes = pathNodes.copyOfRange(0, count * 2)
        }

        pathNodes[count] = node
        node.heapIndex = count
        shiftUp(count++)
        return node
    }

    fun clear() {
        count = 0
    }

    fun pop(): PathNode {
        val pathNode = checkNotNull(pathNodes[0])
        pathNodes[0] = pathNodes[--count]
        pathNodes[count] = null
        if (count > 0) {
            shiftDown(0)
        }
        pathNode.heapIndex = -1
        return pathNode
    }

    fun setNodeWeight(node: PathNode, weight: Float) {
        val oldWeight = node.heapWeight
        node.heapWeight = weight
        if (weight < oldWeight) {
            shiftUp(node.heapIndex)
        } else {
            shiftDown(node.heapIndex)
        }
    }

    private fun shiftUp(index: Int) {
        var a = index
        val pathNode = checkNotNull(pathNodes[a])
        var b: Int
        val heapWeight = pathNode.heapWeight
        while (a > 0) {
            b = (a - 1) / 2
            val pathNode2 = checkNotNull(pathNodes[b])
            if (heapWeight >= pathNode2.heapWeight) {
                break
            }
            pathNodes[a] = pathNode2
            pathNode2.heapIndex = a
            a = b
        }
        pathNodes[a] = pathNode
        pathNode.heapIndex = a
    }

    private fun shiftDown(index: Int) {
        var a = index
        val pathNode = checkNotNull(pathNodes[a])
        val heapWeight = pathNode.heapWeight
        while (true) {
            val c = 1 + (a * 2)
            val d = c + 1
            if (c >= count) {
                break
            }
            val pathNode2 = checkNotNull(pathNodes[c])
            val heapWeight2 = pathNode2.heapWeight
            var pathNode4: PathNode?
            var heapWeight3: Float
            if (d >= count) {
                pathNode4 = null
                heapWeight3 = Float.POSITIVE_INFINITY
            } else {
                pathNode4 = checkNotNull(pathNodes[d])
                heapWeight3 = pathNode4.heapWeight
            }
            if (heapWeight2 < heapWeight3) {
                if (heapWeight2 >= heapWeight) {
                    break
                }
                pathNodes[a] = pathNode2
                pathNode2.heapIndex = a
                a = c
            } else {
                if (heapWeight3 >= heapWeight) {
                    break
                }
                checkNotNull(pathNode4)
                pathNodes[a] = pathNode4
                pathNode4.heapIndex = a
                a = d
            }
        }
        pathNodes[a] = pathNode
        pathNode.heapIndex = a
    }

    val isEmpty get() = count == 0
}

