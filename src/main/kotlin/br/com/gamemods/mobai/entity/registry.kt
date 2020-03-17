package br.com.gamemods.mobai.entity

import br.com.gamemods.mobai.MobAIPlugin
import br.com.gamemods.mobai.entity.EntityCategory.*
import br.com.gamemods.mobai.entity.monster.SmartCreeper
import br.com.gamemods.mobai.entity.passive.SmartCow
import br.com.gamemods.mobai.entity.passive.SmartMooshroom
import br.com.gamemods.mobai.entity.passive.SmartPig
import br.com.gamemods.mobai.entity.smart.SmartAnimal
import br.com.gamemods.mobai.entity.smart.SmartMonster
import cn.nukkit.entity.Entity
import cn.nukkit.entity.EntityFactory
import cn.nukkit.entity.EntityType
import cn.nukkit.entity.EntityTypes.*
import cn.nukkit.level.ChunkManager
import cn.nukkit.level.Level
import cn.nukkit.level.chunk.Chunk
import cn.nukkit.math.Vector3i
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.plugin.Plugin
import cn.nukkit.registry.EntityRegistry
import cn.nukkit.utils.Identifier
import java.util.*

private val defaultSpawnCondition = object : SpawnCondition {
    override val restriction = SpawnRestriction.NOWHERE
    override val height = HeightMapType.MOTION_BLOCKING_NO_LEAVES

    override fun canSpawn(
        type: EntityType<*>,
        level: Level,
        chunkManager: ChunkManager,
        spawnType: SpawnType,
        spawnPos: Vector3i,
        random: Random
    ): Boolean {
        return false
    }
}
private val spawnConditions = mutableMapOf<EntityType<*>, SpawnCondition>()
private val entityCategories = mutableMapOf<Identifier, EntityCategory>()
private val entitiesInCategory = EnumMap<EntityCategory, MutableSet<EntityType<*>>>(EntityCategory::class.java)

val EntityType<*>.spawnCondition get() = spawnConditions[this] ?: defaultSpawnCondition
val EntityType<*>.category get() = entityCategories[identifier] ?: UNKNOWN

val EntityCategory.entities get() = entitiesInCategory[this]?.toSet() ?: emptySet()

internal fun MobAIPlugin.registerEntities() {
    val plugin = this
    EntityRegistry.get().apply {
        val registerInternal = EntityRegistry::class.java.getDeclaredMethod("registerInternal",
                Plugin::class.java, EntityType::class.java, EntityFactory::class.java, Integer.TYPE, Integer.TYPE, java.lang.Boolean.TYPE)
        registerInternal.isAccessible = true

        fun <T: Entity> registerInNukkit(entityType: EntityType<T>, hasSpawnEgg: Boolean = false, priority: Int = 900, factory: EntityFactory<T>) {
            registerInternal(this, plugin, entityType, factory, -1, priority, hasSpawnEgg)
        }
        fun <T: Entity> registerInNukkit(entityType: EntityType<T>, entityFactory: EntityFactory<T>) = registerInNukkit(entityType, factory = entityFactory)
        fun <T: Entity> register(
            entityType: EntityType<T>,
            factory: (EntityType<T>, Chunk, CompoundTag) -> T,
            spawnCondition: SpawnCondition,
            category: EntityCategory = CREATURE
        ) {
            registerInNukkit(entityType, EntityFactory(factory))
            spawnConditions[entityType] = spawnCondition
            entityCategories[entityType.identifier] = category
            entitiesInCategory.getOrPut(category, ::mutableSetOf) += entityType
        }

        register(PIG, ::SmartPig, SmartAnimal)
        register(COW, ::SmartCow, SmartAnimal)
        register(MOOSHROOM, ::SmartMooshroom, SmartMooshroom)
        register(CREEPER, ::SmartCreeper, SmartMonster, MONSTER)
    }
}

abstract class Spawnable(
    override val restriction: SpawnRestriction = SpawnRestriction.ON_GROUND,
    override val height: HeightMapType = HeightMapType.MOTION_BLOCKING_NO_LEAVES
): SpawnCondition

interface SpawnCondition {
    val restriction: SpawnRestriction
    val height: HeightMapType
    fun canSpawn(
        type: EntityType<*>,
        level: Level,
        chunkManager: ChunkManager,
        spawnType: SpawnType,
        spawnPos: Vector3i,
        random: Random
    ): Boolean
}

enum class SpawnRestriction {
    ON_GROUND,
    IN_WATER,
    ANYWHERE,
    NOWHERE
}

enum class HeightMapType {
    MOTION_BLOCKING,
    MOTION_BLOCKING_NO_LEAVES
}

@Deprecated("In favor of Nukkit's SpawnReason",
    replaceWith = ReplaceWith("SpawnReason", "cn.nukkit.event.entity.CreatureSpawnEvent.SpawnReason"))
enum class SpawnType {
    NATURAL,
    CHUNK_GENERATION,
    SPAWNER,
    STRUCTURE,
    BREEDING,
    MOB_SUMMONED,
    JOCKEY,
    EVENT,
    CONVERSION,
    REINFORCEMENT,
    TRIGGERED,
    BUCKET,
    SPAWN_EGG,
    COMMAND,
    DISPENSER,
    PATROL
}
