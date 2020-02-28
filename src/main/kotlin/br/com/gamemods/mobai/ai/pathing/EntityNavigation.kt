package br.com.gamemods.mobai.ai.pathing

import br.com.gamemods.mobai.entity.attribute
import br.com.gamemods.mobai.entity.smart.EntityAI
import br.com.gamemods.mobai.level.ChunkCache
import cn.nukkit.entity.Attribute
import cn.nukkit.math.NukkitMath
import cn.nukkit.math.Vector3f
import cn.nukkit.math.Vector3i

abstract class EntityNavigation(protected val ai: EntityAI) {
    protected var currentPath: Path? = null
    var speed = 0.0
    private val followRange = ai.entity.attribute(Attribute.FOLLOW_RANGE)
    protected var tickCount = 0
    protected var field_6674 = 0
    protected var field_6672 = Vector3f()
    protected var field_6680 = Vector3f()
    protected var field_6670 = 0L
    protected var field_6669 = 0L
    protected var field_6682 = 0.0
    protected var field_6683 = 0.5F
    var shouldRecalculate = false; protected set
    protected var lastRecalculateTime = 0
    protected abstract var nodeMaker: PathNodeMaker
    var currentTarget: Vector3i? = null; private set
    private var _distance = 0
    var rangeMultiplier = 1F
    protected val navigationRange = NukkitMath.floorDouble(followRange.value * 16.0)
    protected abstract val pathNodeNavigator: PathNodeNavigator
    protected abstract val isAtValidPosition: Boolean

    open val isIdle get() = true

    fun resetRangeMultiplier() {
        rangeMultiplier = 1F
    }

    fun recalculatePath() {
        val time = ai.entity.level.time
        if (time - lastRecalculateTime > 20) {
            currentTarget?.also { target ->
                currentPath = null
                currentPath = findPathTo(target, _distance)
                lastRecalculateTime = time
                shouldRecalculate = false
            }
        } else {
            shouldRecalculate = true
        }
    }

    fun findPathTo(target: Vector3i, distance: Int) = findPathToAny(setOf(target), 8, false, distance)
    protected fun findPathToAny(positions: Set<Vector3i>, range: Int, above: Boolean, distance: Int): Path? {
        val entity = ai.entity
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
        val chunkCache = ChunkCache(entity.level, pos.subtract(-finalRange, -finalRange, -finalRange), pos.add(finalRange, finalRange, finalRange))
        val path: Path? = pathNodeNavigator.findPathToAny(chunkCache, ai, entity, positions, followRange, distance, rangeMultiplier)
        if (path?.target != null) {
            currentTarget = path.target
            _distance = distance
        }
        return path
    }

    abstract fun tick(): Boolean
}
