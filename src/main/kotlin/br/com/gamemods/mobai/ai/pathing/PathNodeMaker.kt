package br.com.gamemods.mobai.ai.pathing

import br.com.gamemods.mobai.entity.smart.EntityAI
import br.com.gamemods.mobai.level.ChunkCache
import br.com.gamemods.mobai.math.intFloor
import cn.nukkit.entity.Entity
import cn.nukkit.level.ChunkManager
import cn.nukkit.math.Vector3f
import cn.nukkit.math.Vector3i

abstract class PathNodeMaker {
    protected val pathNodeCache = mutableMapOf<Vector3i, PathNode>()
    protected var chunkCache: ChunkCache? = null
    protected var entity: Entity? = null
    protected var ai: EntityAI<*>? = null
    protected var sizeX = 0
    protected var sizeY = 0
    protected var sizeZ = 0
    var canEnterOpenDoors = false
    var canOpenDoors = false
    var canSwim = false

    abstract val start: PathNode

    open fun init(chunkCache: ChunkCache, ai: EntityAI<*>, entity: Entity) {
        this.chunkCache = chunkCache
        this.ai = ai
        this.entity = entity
        pathNodeCache.clear()
        sizeX = (entity.width + 1).intFloor()
        sizeY = (entity.height + 1).intFloor()
        sizeZ = sizeX
    }

    open fun clear() {
        chunkCache = null
        entity = null
        ai = null
    }

    open fun createNode(pos: Vector3i) = pathNodeCache.computeIfAbsent(pos, ::PathNode)
    abstract fun createTargetNode(pos: Vector3f): TargetPathNode

    abstract fun successors(successors: Array<PathNode?>, node: PathNode): Int
    abstract fun nodeType(
        levelView: ChunkManager,
        pos: Vector3i,
        entity: Entity,
        sizeX: Int,
        sizeY: Int,
        sizeZ: Int,
        canOpenDoors: Boolean,
        canEnterOpenDoors: Boolean
    ): PathNodeType

    protected abstract fun adjustNodeType(
        levelView: ChunkManager,
        canOpenDoors: Boolean,
        canEnterOpenDoors: Boolean,
        pos: Vector3i,
        type: PathNodeType
    ): PathNodeType

    abstract fun nodeType(levelView: ChunkManager, pos: Vector3i, entity: Entity): PathNodeType
}
