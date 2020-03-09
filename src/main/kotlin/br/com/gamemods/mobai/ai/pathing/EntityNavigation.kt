package br.com.gamemods.mobai.ai.pathing

import br.com.gamemods.mobai.entity.attribute
import br.com.gamemods.mobai.entity.eyePosition
import br.com.gamemods.mobai.entity.movementSpeed
import br.com.gamemods.mobai.entity.smart.EntityAI
import br.com.gamemods.mobai.entity.smart.SmartEntity
import br.com.gamemods.mobai.level.ChunkCache
import br.com.gamemods.mobai.level.isAir
import br.com.gamemods.mobai.math.MobAiMath
import br.com.gamemods.mobai.math.intFloor
import cn.nukkit.block.BlockIds
import cn.nukkit.block.BlockLiquid
import cn.nukkit.entity.Attribute
import cn.nukkit.entity.Entity
import cn.nukkit.entity.impl.BaseEntity
import cn.nukkit.math.Vector3f
import cn.nukkit.math.Vector3i
import kotlin.math.abs

abstract class EntityNavigation<T>(val ai: EntityAI<T>) where T: SmartEntity, T: BaseEntity {
    protected val entity get() = ai.entity
    protected var currentPath: Path? = null
    var speed = 0.0
    private val followRange = entity.attribute(Attribute.FOLLOW_RANGE)
    protected var tickCount = 0
    protected var lastUpdateTick = 0
    protected var lastEntityPos = Vector3f()
    protected var lastPathPos = Vector3f()
    protected var nanoTimeSpent = 0L
    protected var lastNanoTimeCheck = 0L
    protected var expectedTime = 0.0
    protected var nodeReachProximity = 0.5F
    var shouldRecalculate = false; protected set
    protected var lastRecalculateTime = 0
    abstract val nodeMaker: PathNodeMaker<T>
    var currentTarget: Vector3i? = null; private set
    private var targetDistance = 0
    var rangeMultiplier = 1F

    protected val navigationRange get() = (followRange.value * 16.0).intFloor()
    protected abstract val pathNodeNavigator: PathNodeNavigator<T>

    protected abstract val isAtValidPosition: Boolean

    protected abstract val position: Vector3f

    val isIdle get() = currentPath?.isFinished != false
    val isActive get() = !isIdle
    val isInLiquid get() = entity.let { entity ->
        entity.level.getLoadedBlock(entity.eyePosition).let { block ->
            block is BlockLiquid || block?.getBlockAtLayer(1) is BlockLiquid
        }
    }

    fun resetRangeMultiplier() {
        rangeMultiplier = 1F
    }

    fun recalculatePath() {
        val time = entity.level.time
        if (time - lastRecalculateTime > 20) {
            currentTarget?.also { target ->
                currentPath = null
                currentPath = findPathTo(target, targetDistance)
                lastRecalculateTime = time
                shouldRecalculate = false
            }
        } else {
            shouldRecalculate = true
        }
    }

    open fun findPathTo(entity: Entity, distance: Int) = findPathToAny(setOf(entity.position.asVector3i()), 16, true, distance)
    open fun findPathTo(target: Vector3i, distance: Int) = findPathToAny(setOf(target), 8, false, distance)

    protected fun findPathToAny(positions: Set<Vector3i>, range: Int, above: Boolean, distance: Int): Path? {
        if (positions.isEmpty() || entity.y < 0.0 || !isAtValidPosition) {
            return null
        }

        currentPath?.let { path ->
            if (!path.isFinished && currentTarget in positions) {
                return path
            }
        }

        val followRange = followRange.value
        val pos = entity.asVector3i().run {
            if (above) up() else this
        }
        val finalRange = (followRange + range).toInt()
        val chunkCache = ChunkCache(entity.level, pos.subtract(finalRange, finalRange, finalRange), pos.add(finalRange, finalRange, finalRange))
        val path: Path? = pathNodeNavigator.findPathToAny(chunkCache,
            ai, entity, positions, followRange, distance, rangeMultiplier)
        if (path?.target != null) {
            currentTarget = path.target
            targetDistance = distance
        }
        return path
    }

    fun startMovingTo(pos: Vector3i, speed: Double) = startMovingAlong(findPathTo(pos, 1), speed)
    fun startMovingTo(entity: Entity, speed: Double): Boolean {
        val path = findPathTo(entity, 1) ?: return false
        return startMovingAlong(path, speed)
    }

    fun startMovingAlong(path: Path?, speed: Double): Boolean {
        if (path == null) {
            currentPath = null
            return false
        }

        if (!path.equalsPath(currentPath)) {
            debugClear()
            currentPath = path
            debugPath()
        }

        val currentPath = currentPath ?: return false
        if (isIdle) {
            return false
        }

        validatePath(currentPath)
        if (currentPath.size <= 0) {
            return false
        }

        this.speed = speed
        lastUpdateTick = tickCount
        lastEntityPos = position
        return true
    }

    protected open fun validatePath(path: Path) {
        val level = entity.level
        path.forEachIndexed { index, pathNode ->
            val nextNode = path.getOrNull(index + 1)
            val block = level.getBlock(pathNode)
            if (block.id == BlockIds.CAULDRON) {
                path[index] = pathNode.copy(x= pathNode.x, y= pathNode.y + 1, z= pathNode.z)
                if (nextNode != null && pathNode.y >= nextNode.y) {
                    path[index + 1] = nextNode.copy(x= nextNode.x, y= pathNode.y + 1, z= nextNode.z)
                }
            }
        }
    }

    fun tick(): Boolean {
        tickCount++
        if (shouldRecalculate) {
            recalculatePath()
        }

        if (isIdle) {
            return true
        }

        val currentPath = checkNotNull(currentPath)

        if (isAtValidPosition) {
            updateProgress()
        } else if (currentPath.currentNodeIndex < currentPath.size) {
            val entityPos = position
            val pathPos = currentPath.nodePosition(entity, currentPath.currentNodeIndex)
            if (entityPos.y > pathPos.y
                && !entity.isOnGround
                && entityPos.x.intFloor() == pathPos.x.intFloor()
                && entityPos.z.intFloor() == pathPos.z.intFloor()
            ) {
                currentPath.currentNodeIndex++
            }
        }

        if (isActive) {
            val targetPos = currentPath.nodePosition(entity)
            val blockPos = targetPos.asVector3i()
            if (!entity.level.getBlock(blockPos.down()).isAir) {
                targetPos.y = LandPathNodeMaker.getHeight(entity.level, blockPos)
            }
            entity.ai.moveControl.moveTo(targetPos, speed)
        }

        return true
    }

    protected open fun updateProgress() {
        val currentPath = checkNotNull(currentPath)

        val pos = position
        val nodeReachProximity = entity.width.let { if (it > 0.75f) it / 2.0f else 0.75f - it / 2.0f }.let {
            this.nodeReachProximity = it
            it.toDouble()
        }
        val currentPos = currentPath.currentPosition
        if (abs(entity.x - (currentPos.x + 0.5)) < nodeReachProximity
            && abs(entity.z - (currentPos.z + 0.5)) < nodeReachProximity
            && abs(entity.y - currentPos.y) < 1.0
        ) {
            currentPath.currentNodeIndex++
        }

        checkStop(pos)
    }

    protected open fun checkStop(entityPos: Vector3f) {
        if (tickCount - lastUpdateTick > 100) {
            if (entityPos.distanceSquared(lastEntityPos) < 2.25) {
                stop()
                return
            }

            lastUpdateTick = tickCount
            lastEntityPos = entityPos
        }

        val currentPath = currentPath ?: return
        if (currentPath.isFinished) {
            return
        }

        val currentPathPos = currentPath.currentPosition
        if (currentPathPos == lastPathPos) {
            nanoTimeSpent += MobAiMath.nanoTime() - lastNanoTimeCheck
        } else {
            lastPathPos = currentPathPos
            val distance = entityPos.distance(currentPathPos)
            val movementSpeed = entity.movementSpeed
            this.expectedTime = if(movementSpeed > 0F) distance / (movementSpeed * 1000) else 0.0
        }

        if (expectedTime > 0.0 && nanoTimeSpent > expectedTime * 3.0) {
            lastPathPos = Vector3f()
            nanoTimeSpent = 0L
            expectedTime = 0.0
            stop()
        }

        lastNanoTimeCheck = System.nanoTime()
    }

    fun stop() {
        debugClear()
        currentPath = null
    }

    @Deprecated("Debug")
    fun debugClear() {
        //currentPath?.forEach { entity.level.setBlock(it, Block.get(BlockIds.AIR)) }
    }

    @Deprecated("Debug")
    fun debugPath() {
        /*val path = currentPath ?: return
        path.forEach {
            entity.level.setBlock(it, Block.get(BlockIds.REDSTONE_WIRE))
        }*/
        //entity.level.setBlock(path.first(), Block.get(BlockIds.CARPET))
        //entity.level.setBlock(path.last(), Block.get(BlockIds.WOODEN_SLAB))
    }

    //TODO isFullOpaque should really be translated to isSolid?
    open fun isValidPosition(pos: Vector3i) = entity.level.getBlock(pos.down()).isSolid
}
