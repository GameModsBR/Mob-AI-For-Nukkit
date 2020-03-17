package br.com.gamemods.mobai.level.spawning

import br.com.gamemods.mobai.MobAIPlugin
import br.com.gamemods.mobai.entity.*
import br.com.gamemods.mobai.entity.EntityCategory.CREATURE
import br.com.gamemods.mobai.entity.smart.EntityProperties
import br.com.gamemods.mobai.entity.smart.SmartEntity
import br.com.gamemods.mobai.entity.smart.logic.type
import br.com.gamemods.mobai.level.*
import br.com.gamemods.mobai.math.chunkIndex
import br.com.gamemods.mobai.math.intCeil
import br.com.gamemods.mobai.math.isWithinDistance
import br.com.gamemods.mobai.math.square
import cn.nukkit.Server
import cn.nukkit.block.BlockRail
import cn.nukkit.entity.Entity
import cn.nukkit.entity.EntityType
import cn.nukkit.entity.impl.BaseEntity
import cn.nukkit.level.Level
import cn.nukkit.level.Position
import cn.nukkit.level.chunk.Chunk
import cn.nukkit.level.chunk.IChunk
import cn.nukkit.level.gamerule.GameRules
import cn.nukkit.math.Vector2f
import cn.nukkit.math.Vector3f
import cn.nukkit.math.Vector3i
import cn.nukkit.registry.EntityRegistry
import org.slf4j.Logger
import java.util.*
import java.util.concurrent.ThreadLocalRandom

object NaturalSpawnTask: Runnable {
    private var current: Iterator<String>? = null
    internal lateinit var logger: Logger
    private val categories = EntityCategory.values().asSequence()
        .filterNot { it.isMiscOrUnknown }
        .toList()

    private fun Level.getMobCounts(): MobCount {
        return MobCount(
            entities.asSequence()
                .mapNotNull { it as? SmartEntity }
                .filterNot { it.isPersistent || it.cannotDespawn() }
                .toList()
        )
    }

    override fun run() {
        //AiTimings.naturalSpawner.track {
            Server.getInstance().levels.forEach { it.skyLightSubtractedFixed = it.calculateSkylightSubtractedFixed().toFloat() }
            tick()
        //}
    }

    fun tick() {
        val server = Server.getInstance()
        server.levels.filter { it.gameRules[GameRules.DO_MOB_SPAWNING] && (it.spawnMonsters || it.spawnAnimals) }.random().let {
            try {
                if (server.isLevelLoaded(it.name)) {
                    tickNaturalSpawn(it)
                }
            } catch (e: Exception) {
                logger.error("Failed to tick natural spawn of ${it.name}", e)
            }
        }
    }

    private val Level.isLoaded get() = server.isLevelLoaded(name)
    private val IChunk.isLoaded get() = level.getLoadedChunk(x, z) === this

    private fun tickNaturalSpawn(level: Level) {
        if (!level.gameRules[GameRules.DO_MOB_SPAWNING] || (!level.spawnMonsters && !level.spawnAnimals)) {
            return
        }

        val chunkPosList = (level.players.values.takeIf { it.isNotEmpty() } ?: return).asSequence()
            .flatMap { player ->
                (-8..8).asSequence().flatMap { x->
                    (-8..8).asSequence().map { z ->
                        (player.chunkX + x) to (player.chunkZ + z)
                    }
                }
            }
            .distinct()
            .toList()

        val chunk = chunkPosList.takeIf { it.isNotEmpty() }
            ?.random()
            ?.let { (x, z) -> level.getLoadedChunk(x, z) }
            ?.let(::CachingChunk)
            ?: return
        /*val chunkCount = players.values.let { players ->
            chunks.count { chunk ->
                if (chunk.players.isNotEmpty()) return@count true
                players.any { it.isOnline && it.distanceSquared(chunk) <= 8 }
            }
        }*/

        val chunkCount = chunkPosList.size

        val mobCount = level.getMobCounts()

        val random: Random = ThreadLocalRandom.current()

        val spawnLocation = level.spawnLocation
        val cache = OnDemandChunkManager(level, chunk)
        categories.forEach { category ->
            try {
                tickCategory(cache, level, category, spawnLocation, chunkCount, mobCount, random, chunk)
            } catch (e: Exception) {
                logger.error("Failed to tick the natural spawn category $category of ${level.name}", e)
            }
        }
    }

    private fun tickCategory(
        chunkManager: OnDemandChunkManager,
        level: Level,
        category: EntityCategory,
        spawnLocation: Position,
        chunkCount: Int,
        mobCount: MobCount,
        random: Random,
        chunk: CachingChunk
    ) {
        with(level) {
            if (category.isPeaceful && !spawnAnimals
                || !category.isPeaceful && !spawnMonsters
                || category.isAnimal && isNight
            ) {
                return
            }
        }

        val effectiveMobCap = category.getEffectiveCap(level) * chunkCount / 17.square()
        val categoryCount = mobCount.byCategory[category] ?: 0
        if (categoryCount > effectiveMobCap) {
            return
        }

        val randomPos = chunk.run {
            val x = random.nextInt(16)
            val z = random.nextInt(16)
            val surface = getHeightMap(x, z) + 1
            val y = random.nextInt(surface + 1)
            if (y < 1) {
                return
            }
            Vector3i((this.x shl 4) + x, y, (this.z shl 4) + z)
        }

        val block = chunk.getBlock(randomPos.chunkIndex())
        if (block.isNormalBlock) {
            return
        }

        var totalCount = 0
        val mutable = block.asVector3i()
        type@ for (i in 0..2) {
            var spawnEntry: SpawnEntry? = null
            var groupData: Any? = null
            var spawnCount = 0
            var groupSize = (random.nextDouble() * 4).intCeil()
            var currentTry = 0
            group@ while (currentTry++ < groupSize) {

                mutable.x += random.nextInt(6) - random.nextInt(6)
                mutable.z += random.nextInt(6) - random.nextInt(6)
                val planePos = Vector2f(mutable.x + 0.5, mutable.z + 0.5)
                val closestPlayer = level.findPlayers(planePos).closest()?.first ?: continue@group

                val tridimensionalPos = Vector3f(planePos.x, mutable.y.toDouble(), planePos.y)
                val distanceSquared = closestPlayer.distanceSquared(tridimensionalPos)
                if (distanceSquared <= 24.square()
                    || spawnLocation.isWithinDistance(tridimensionalPos, 24.0)) {
                    continue@group
                }

                val currentChunk = if (mutable.chunkX == chunk.x && mutable.chunkZ == chunk.z) {
                    chunk
                } else {
                    chunkManager.getChunk(mutable)?.takeIf { it.isGenerated }?.let(::CachingChunk) ?: continue@group
                }

                val currentChunkIndex = mutable.chunkIndex()
                if (spawnEntry == null) {
                    spawnEntry = category.pickRandomSpawnEntry(currentChunk, currentChunkIndex, mutable, random)
                        ?: continue@type
                    groupSize = spawnEntry.minGroupSize + random.nextInt(1 + spawnEntry.maxGroupSize - spawnEntry.minGroupSize)
                }

                val entityType = spawnEntry.type
                val categoryToSpawn = entityType.category
                if (categoryToSpawn.isMiscOrUnknown
                    || categoryToSpawn === CREATURE && distanceSquared > 128.square()
                    || !category.containsSpawnEntry(currentChunk, currentChunkIndex, mutable, spawnEntry)) {
                    continue@group
                }

                if(!entityType.canSpawn(currentChunk, currentChunkIndex)) {
                    continue@group
                }

                if(!entityType.spawnCondition.canSpawn(entityType, level,
                        chunkManager, SpawnType.NATURAL, mutable, random)) {
                    continue@group
                }

                //TODO !serverWorld.doesNotCollide(entityType.createSimpleBoundingBox(f, k, g))
                val entity = try {
                    EntityRegistry.get().newEntity(
                        entityType,
                        MobAIPlugin.INSTANCE,
                        currentChunk.parent as? Chunk ?: level.getChunk(currentChunk.x, currentChunk.z),
                        Entity.getDefaultNBT(tridimensionalPos, null, random.nextFloat() * 360, 0F)
                    ) ?: continue@type
                } catch (e: Exception) {
                    logger.error("Failed to spawn entity $entityType", e)
                    return
                }

                if (entity is SmartEntity) {
                    if (distanceSquared > 128.square() && entity.canDespawnImmediately(distanceSquared)
                        || !entity.canSpawn(SpawnType.NATURAL)
                        || !entity.canSpawn()) {
                        entity.close()
                        continue@group
                    }
                    groupData = entity.postSpawn(SpawnType.NATURAL, groupData, random)
                }
                if (entity is BaseEntity) {
                    entity.scheduleUpdate()
                }
                spawnCount++
                totalCount++
                entity.spawnToAll()
                if (totalCount >= ((entity as? EntityProperties)?.limitPerChunk ?: 4)) {
                    return
                }
                if ((entity as? SmartEntity)?.spawnsTooManyForEachTry(spawnCount) != false) {
                    continue@type
                }
            }
        }
    }

    private fun EntityType<*>.canSpawn(chunk: IChunk, chunkIndex: Vector3i): Boolean {
        val spawnCondition = spawnCondition
        return when (spawnCondition.restriction) {
            SpawnRestriction.ANYWHERE -> true
            SpawnRestriction.NOWHERE -> false
            SpawnRestriction.IN_WATER ->
                chunk.isFlooded(chunkIndex)
                        && chunk.isFlooded(chunkIndex.down())
                        && !chunk[chunkIndex.up()].isNormalBlock
            SpawnRestriction.ON_GROUND ->
                chunk[chunkIndex.down()].allowsSpawning(this)
                        && chunk.isClearForSpawn(chunkIndex)
                        && chunk.isClearForSpawn(chunkIndex.up())
        }

    }

    private fun IChunk.isClearForSpawn(pos: Vector3i): Boolean {
        if (isFlooded(pos)) {
            return false
        }
        val block = getBlock(pos)
        return !(block.isPowerSource || block.isSolid || block is BlockRail)
    }


    private class MobCount(val counted: List<SmartEntity>) {
        val byCategory by lazy {
            counted.groupingBy { it.type.category }.eachCount()
        }

        val byType by lazy {
            counted.groupingBy { it.type.identifier }.eachCount()
        }
    }
}
