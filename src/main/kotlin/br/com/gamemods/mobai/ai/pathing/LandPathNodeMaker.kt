package br.com.gamemods.mobai.ai.pathing

import br.com.gamemods.mobai.entity.isTouchingWater
import br.com.gamemods.mobai.entity.smart.EntityAI
import br.com.gamemods.mobai.entity.smart.SmartEntity
import br.com.gamemods.mobai.level.*
import br.com.gamemods.mobai.level.FutureBlockIds.BLUE_FIRE
import br.com.gamemods.mobai.math.intFloor
import cn.nukkit.block.*
import cn.nukkit.block.BlockIds.*
import cn.nukkit.entity.Entity
import cn.nukkit.entity.impl.BaseEntity
import cn.nukkit.level.BlockPosition
import cn.nukkit.level.ChunkManager
import cn.nukkit.math.BlockFace
import cn.nukkit.math.SimpleAxisAlignedBB
import cn.nukkit.math.Vector3f
import cn.nukkit.math.Vector3i
import java.util.*
import kotlin.math.max

open class LandPathNodeMaker<E>: PathNodeMaker<E>() where E: SmartEntity, E: BaseEntity{
    var waterPathNodeTypeWeight = PathNodeType.WATER.defaultPenalty

    override fun init(chunkCache: ChunkCache, ai: EntityAI<E>, entity: E) {
        super.init(chunkCache, ai, entity)
        waterPathNodeTypeWeight = entity.pathFindingPenalty(PathNodeType.WATER)
    }

    override val start: PathNode get() {
        val entity = checkNotNull(entity)
        val chunkCache = checkNotNull(chunkCache)
        var y: Int
        if (canSwim && entity.isTouchingWater) {
            val x = entity.x.intFloor()
            y = entity.y.intFloor()
            val z = entity.z.intFloor()
            val mutable = BlockPosition(x, y, z)
            while (chunkCache.getWaterDamage(mutable) != -1) {
                mutable.y = ++y
            }
            y--
        } else if (entity.isOnGround) {
            y = (entity.y + 0.5).intFloor()
        } else {
            val pos = entity.position.asVector3i()
            while (pos.y > 0 && chunkCache.getBlock(pos).let { it.isAir || it.canEntityPathFind(entity, PathFindEnvironment.LAND) }) {
                pos.y--
            }
            y = pos.y + 1
        }
        val entityPos = entity.position.asVector3i()
        entityPos.y = y
        val pathNodeType = nodeType(entity, entityPos)
        if (entity.pathFindingPenalty(pathNodeType) < 0.0f) {
            val boundingBox = entity.boundingBox

            for(blockPos3 in arrayOf(
                Vector3i(boundingBox.minX.intFloor(), y, boundingBox.minZ.intFloor()),
                Vector3i(boundingBox.minX.intFloor(), y, boundingBox.maxZ.intFloor()),
                Vector3i(boundingBox.maxX.intFloor(), y, boundingBox.minZ.intFloor()),
                Vector3i(boundingBox.maxX.intFloor(), y, boundingBox.maxZ.intFloor())
            )) {
                val pathNodeType2: PathNodeType = nodeType(entity, blockPos3)
                if (entity.pathFindingPenalty(pathNodeType2) >= 0.0f) {
                    return createNode(blockPos3)
                }
            }
        }
        return this.createNode(entityPos)
    }

    private fun nodeType(entity: E, pos: Vector3i) = nodeType(entity, pos.x, pos.y, pos.z)

    private fun nodeType(entity: E, x: Int, y: Int, z: Int): PathNodeType {
        return nodeType(checkNotNull(chunkCache), Vector3i(x, y, z), entity, sizeX, sizeY, sizeZ, canOpenDoors, canEnterOpenDoors)
    }

    override fun clear() {
        ai?.entity?.pathFindingPenalties?.put(PathNodeType.WATER, waterPathNodeTypeWeight)
        super.clear()
    }

    override fun successors(successors: Array<PathNode?>, node: PathNode): Int {
        val entity = checkNotNull(entity)
        val chunkCache = checkNotNull(chunkCache)
        var index = 0
        var maxYStep = 0
        val pathNodeType = nodeType(entity, node.x, node.y + 1, node.z)
        if (entity.pathFindingPenalty(pathNodeType) >= 0.0f) {
            val pathNodeType2 = nodeType(entity, node.x, node.y, node.z)
            maxYStep = if (pathNodeType2 == PathNodeType.STICKY_HONEY) {
                0
            } else {
                1F.coerceAtLeast(entity.stepHeight).intFloor()
            }
        }

        val height = getHeight(chunkCache, node)
        val south = getPathNode(Vector3i(node.x, node.y, node.z + 1), maxYStep, height, BlockFace.SOUTH)
        if (south != null && !south.visited && south.penalty >= 0.0f) {
            successors[index++] = south
        }

        val west = getPathNode(Vector3i(node.x - 1, node.y, node.z), maxYStep, height, BlockFace.WEST)
        if (west != null && !west.visited && west.penalty >= 0.0f) {
            successors[index++] = west
        }

        val east = getPathNode(Vector3i(node.x + 1, node.y, node.z), maxYStep, height, BlockFace.EAST)
        if (east != null && !east.visited && east.penalty >= 0.0f) {
            successors[index++] = east
        }

        val north = getPathNode(Vector3i(node.x, node.y, node.z - 1), maxYStep, height, BlockFace.NORTH)
        if (north != null && !north.visited && north.penalty >= 0.0f) {
            successors[index++] = north
        }

        val northwest = getPathNode(Vector3i(node.x - 1, node.y, node.z - 1), maxYStep, height, BlockFace.NORTH)
        if (isValidDiagonalSuccessor(node, west, north, northwest)) {
            successors[index++] = northwest
        }

        val northeast = getPathNode(Vector3i(node.x + 1, node.y, node.z - 1), maxYStep, height, BlockFace.NORTH)
        if (isValidDiagonalSuccessor(node, east, north, northeast)) {
            successors[index++] = northeast
        }

        val southwest = getPathNode(Vector3i(node.x - 1, node.y, node.z + 1), maxYStep, height, BlockFace.SOUTH)
        if (isValidDiagonalSuccessor(node, west, south, southwest)) {
            successors[index++] = southwest
        }

        val southeast = getPathNode(Vector3i(node.x + 1, node.y, node.z + 1), maxYStep, height, BlockFace.SOUTH)
        if (isValidDiagonalSuccessor(node, east, south, southeast)) {
            successors[index++] = southeast
        }

        return index
    }

    private fun isValidDiagonalSuccessor(node: PathNode, successor1: PathNode?, successor2: PathNode?, diagonalSuccessor: PathNode?): Boolean {
        if (diagonalSuccessor == null || successor1 == null || successor2 == null) {
            return false
        }

        return when {
            diagonalSuccessor.visited -> false
            successor2.y <= node.y && successor1.y <= node.y -> {
                diagonalSuccessor.penalty >= 0.0f
                        && (successor2.y < node.y || successor2.penalty >= 0.0f)
                        && (successor1.y < node.y || successor1.penalty >= 0.0f)
            }
            else -> false
        }
    }

    private fun getPathNode(blockPos: Vector3i, maxYStep: Int, height: Double, direction: BlockFace): PathNode? {
        val entity = checkNotNull(entity)
        val chunkCache = checkNotNull(chunkCache)
        var pathNode: PathNode? = null
        val height2 = getHeight(chunkCache, blockPos)
        if (height2 - height > 1.125) {
            return null
        }

        var pathNodeType = nodeType(entity, blockPos)
        var penalty: Float = entity.pathFindingPenalty(pathNodeType)
        val entityCenter = entity.width / 2.0
        if (penalty >= 0.0f) {
            pathNode = createNode(blockPos)
            pathNode.type = pathNodeType
            pathNode.penalty = max(pathNode.penalty, penalty)
        }

        if (pathNodeType == PathNodeType.WALKABLE) {
            return pathNode
        }

        check(entity is BaseEntity) { "The entity does not overrides BaseEntity! $entity" }

        if ((pathNode == null || pathNode.penalty < 0.0f)
            && maxYStep > 0
            && pathNodeType != PathNodeType.FENCE
            && pathNodeType != PathNodeType.TRAPDOOR
        ) {
            pathNode = getPathNode(blockPos.up(), maxYStep - 1, height, direction)
            if (pathNode != null
                && (pathNode.type == PathNodeType.OPEN || pathNode.type == PathNodeType.WALKABLE)
                && entity.width < 1.0f
            ) {
                val x = blockPos.x - direction.xOffset + 0.5
                val z = blockPos.z - direction.zOffset + 0.5
                val box = SimpleAxisAlignedBB(
                    x - entityCenter,
                    getHeight(chunkCache, blockPos.up()) + 0.001,
                    z - entityCenter,
                    x + entityCenter,
                    entity.height + getHeight(chunkCache, pathNode) - 0.002,
                    z + entityCenter
                )
                if (chunkCache.level.hasCollision(entity, box, true)) {
                    pathNode = null
                }
            }
        }

        val newPos = blockPos.clone()
        if (pathNodeType == PathNodeType.WATER && !canSwim) {
            if (nodeType(entity, blockPos.down()) != PathNodeType.WATER) {
                return pathNode
            }
            while (newPos.y-- > 0) {
                pathNodeType = nodeType(entity, newPos)
                if (pathNodeType != PathNodeType.WATER) {
                    return pathNode
                }
                pathNode = createNode(newPos)
                pathNode.type = pathNodeType
                pathNode.penalty = max(pathNode.penalty, entity.pathFindingPenalty(pathNodeType))
            }
        }
        if (pathNodeType == PathNodeType.OPEN) {
            val box2 = SimpleAxisAlignedBB(
                newPos.x - entityCenter + 0.5,
                newPos.y + 0.001,
                newPos.z - entityCenter + 0.5,
                newPos.x + entityCenter + 0.5,
                newPos.y.toDouble() + entity.height,
                newPos.z + entityCenter + 0.5
            )
            if (chunkCache.level.hasCollision(entity, box2, true)) {
                return null
            }
            if (entity.width >= 1.0f) {
                val pathNodeType2 = nodeType(entity, newPos.down())
                if (pathNodeType2 == PathNodeType.BLOCKED) {
                    pathNode = createNode(newPos)
                    pathNode.type = PathNodeType.WALKABLE
                    pathNode.penalty = max(pathNode.penalty, penalty)
                    return pathNode
                }
            }
            var fallDistance = 0
            val currentPos = newPos.clone()
            while (pathNodeType == PathNodeType.OPEN) {
                currentPos.y--
                var pathNode3: PathNode
                if (currentPos.y < 0) {
                    pathNode3 = createNode(newPos)
                    pathNode3.type = PathNodeType.BLOCKED
                    pathNode3.penalty = -1.0f
                    return pathNode3
                }
                pathNode3 = createNode(currentPos)
                if (fallDistance++ >= entity.safeFallDistance) {
                    pathNode3.type = PathNodeType.BLOCKED
                    pathNode3.penalty = -1.0f
                    return pathNode3
                }
                pathNodeType = nodeType(entity, currentPos)
                penalty = entity.pathFindingPenalty(pathNodeType)
                if (pathNodeType != PathNodeType.OPEN && penalty >= 0.0f) {
                    pathNode = pathNode3
                    pathNode3.type = pathNodeType
                    pathNode3.penalty = max(pathNode3.penalty, penalty)
                    break
                }
                if (penalty < 0.0f) {
                    pathNode3.type = PathNodeType.BLOCKED
                    pathNode3.penalty = -1.0f
                    return pathNode3
                }
            }
        }
        return pathNode
    }

    override fun createTargetNode(pos: Vector3f): TargetPathNode {
        return TargetPathNode(createNode(pos.asVector3i()))
    }

    override fun nodeType(
        levelView: ChunkManager,
        pos: Vector3i,
        entity: E,
        sizeX: Int,
        sizeY: Int,
        sizeZ: Int,
        canOpenDoors: Boolean,
        canEnterOpenDoors: Boolean
    ): PathNodeType {
        val nearbyTypes = EnumSet.noneOf(PathNodeType::class.java)
        var pathNodeType = PathNodeType.BLOCKED
        val blockPos = entity.position.asVector3i()
        pathNodeType = nodeType(
            levelView, pos, entity, sizeX, sizeY, sizeZ, canOpenDoors, canEnterOpenDoors, nearbyTypes, pathNodeType, blockPos
        )
        return if (nearbyTypes.contains(PathNodeType.FENCE)) {
            PathNodeType.FENCE
        } else {
            var pathNodeType2 = PathNodeType.BLOCKED
            nearbyTypes.forEach { pathNodeType3 ->
                if (entity.pathFindingPenalty(pathNodeType3) < 0F) {
                    return pathNodeType3
                }
                if (entity.pathFindingPenalty(pathNodeType3) >= entity.pathFindingPenalty(pathNodeType2)) {
                    pathNodeType2 = pathNodeType3
                }
            }
            if (pathNodeType == PathNodeType.OPEN && entity.pathFindingPenalty(pathNodeType2) == 0F) {
                PathNodeType.OPEN
            } else {
                pathNodeType2
            }
        }
    }

    private fun nodeType(
        levelView: ChunkManager,
        pos: Vector3i,
        entity: Entity,
        sizeX: Int,
        sizeY: Int,
        sizeZ: Int,
        canOpenDoors: Boolean,
        canEnterOpenDoors: Boolean,
        nearbyTypes: EnumSet<PathNodeType>,
        type: PathNodeType,
        blockPos: Vector3i
    ): PathNodeType {
        var resultType = type
        val currentPos = Vector3i()
        for (x in 0 until sizeX) {
            for (y in 0 until sizeY) {
                for (z in 0 until sizeZ) {
                    currentPos.setComponents(x + pos.x, y + pos.y, z + pos.z)
                    val pathNodeType = nodeType(levelView, currentPos, entity).let {
                        adjustNodeType(levelView, canOpenDoors, canEnterOpenDoors, blockPos, it)
                    }
                    if (x == 0 && y == 0 && z == 0) {
                        resultType = pathNodeType
                    }
                    nearbyTypes += pathNodeType
                }
            }
        }
        return resultType
    }

    override fun adjustNodeType(
        levelView: ChunkManager,
        canOpenDoors: Boolean,
        canEnterOpenDoors: Boolean,
        pos: Vector3i,
        type: PathNodeType
    ): PathNodeType = when {
        type == PathNodeType.DOOR_WOOD_CLOSED && canOpenDoors && canEnterOpenDoors -> PathNodeType.WALKABLE
        type == PathNodeType.DOOR_OPEN && !canEnterOpenDoors -> PathNodeType.BLOCKED
        type == PathNodeType.RAIL && levelView.getBlockAt(pos) !is BlockRail
                && levelView[pos.x, pos.y-1, pos.z] !is BlockRail -> PathNodeType.FENCE
        type == PathNodeType.LEAVES -> PathNodeType.BLOCKED
        else -> type
    }

    override fun nodeType(levelView: ChunkManager, pos: Vector3i, entity: Entity): PathNodeType {
        return LandPathNodeMaker.nodeType(levelView, pos, entity)
    }

    companion object {
        fun nodeType(levelView: ChunkManager, pos: Vector3i, entity: Entity): PathNodeType {
            var nodeType = basicNodeType(levelView, pos, entity)
            if (nodeType == PathNodeType.OPEN && pos.y >= 1) {
                val posDown = pos.down()
                val blockDown = levelView.getBlockIdAt(posDown)
                val nodeTypeDown = basicNodeType(levelView, posDown, entity)

                nodeType = when (nodeTypeDown) {
                    PathNodeType.DAMAGE_FIRE, PathNodeType.DAMAGE_CACTUS, PathNodeType.DAMAGE_OTHER, PathNodeType.STICKY_HONEY
                    -> nodeTypeDown
                    else -> when (blockDown) {
                        MAGMA, CAMPFIRE -> PathNodeType.DAMAGE_FIRE
                        else -> {
                            if (nodeTypeDown != PathNodeType.WALKABLE
                                && nodeTypeDown != PathNodeType.OPEN
                                && nodeTypeDown != PathNodeType.WATER
                                && nodeTypeDown != PathNodeType.LAVA
                            ) {
                                PathNodeType.WALKABLE
                            } else {
                                PathNodeType.OPEN
                            }
                        }
                    }
                }
            }

            if (nodeType == PathNodeType.WALKABLE) {
                return detectDanger(levelView, pos, nodeType)
            }
            return nodeType
        }

        fun detectDanger(levelView: ChunkManager, pos: Vector3i, pathNodeType: PathNodeType): PathNodeType {
            for (x in -1..1) {
                for (y in -1..1) {
                    for (z in -1..1) {
                        if (x != 0 || z != 0) {
                            when (levelView.getBlockIdAt(x + pos.x, y + pos.y, z + pos.z)) {
                                FIRE, BLUE_FIRE -> return PathNodeType.DANGER_FIRE
                                CACTUS -> return PathNodeType.DANGER_CACTUS
                                SWEET_BERRY_BUSH -> return PathNodeType.DANGER_OTHER
                            }
                        }
                    }
                }
            }
            return pathNodeType
        }

        fun getHeight(chunkManager: ChunkManager, pos: Vector3i): Double {
            val down = pos.down()
            val bb = chunkManager.getBlockAt(down).boundingBox ?: return down.y.toDouble()
            return bb.maxY
        }

        protected fun basicNodeType(levelView: ChunkManager, pos: Vector3i, entity: Entity): PathNodeType {
            val block = levelView.getBlockAt(pos)
            if (block.isAir) {
                return PathNodeType.OPEN
            }
            if (block is BlockTrapdoor) {
                return PathNodeType.TRAPDOOR
            }
            when (block.id) {
                WATERLILY -> PathNodeType.TRAPDOOR
                FIRE -> PathNodeType.DAMAGE_FIRE
                CACTUS -> PathNodeType.DAMAGE_CACTUS
                SWEET_BERRY_BUSH -> PathNodeType.DAMAGE_OTHER
                HONEY_BLOCK -> PathNodeType.STICKY_HONEY
                COCOA -> PathNodeType.COCOA
                else -> null
            }?.let { return it }

            return when {
                block is BlockDoorWood && !block.isOpen -> PathNodeType.DOOR_WOOD_CLOSED
                block is BlockDoorIron && !block.isOpen -> PathNodeType.DOOR_IRON_CLOSED
                block is BlockDoor && block.isOpen -> PathNodeType.DOOR_OPEN
                block is BlockRail -> PathNodeType.RAIL
                block is BlockLeaves -> PathNodeType.LEAVES
                block is BlockFence || block is BlockWall || block is BlockFenceGate && !block.isOpen -> PathNodeType.FENCE
                block is BlockLava -> PathNodeType.LAVA
                block is BlockWater || block.isWaterlogged -> PathNodeType.WATER
                block.canEntityPathFind(entity, PathFindEnvironment.LAND) -> PathNodeType.OPEN
                else -> PathNodeType.BLOCKED
            }
        }
    }
}
