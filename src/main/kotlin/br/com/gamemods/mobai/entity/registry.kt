package br.com.gamemods.mobai.entity

import br.com.gamemods.mobai.MobAIPlugin
import br.com.gamemods.mobai.entity.passive.SmartPig
import cn.nukkit.entity.Entity
import cn.nukkit.entity.EntityFactory
import cn.nukkit.entity.EntityType
import cn.nukkit.entity.EntityTypes
import cn.nukkit.level.chunk.Chunk
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.plugin.Plugin
import cn.nukkit.registry.EntityRegistry

//private typealias Factory<T> = (EntityType<T>, Chunk, CompoundTag) -> T

internal fun MobAIPlugin.registerEntities() {
    val plugin = this
    EntityRegistry.get().apply {
        val registerInternal = EntityRegistry::class.java.getDeclaredMethod("registerInternal",
                Plugin::class.java, EntityType::class.java, EntityFactory::class.java, Integer.TYPE, Integer.TYPE, java.lang.Boolean.TYPE)
        registerInternal.isAccessible = true

        fun <T: Entity> register(entityType: EntityType<T>, hasSpawnEgg: Boolean = false, priority: Int = 900,
                                 /*factory: Factory<T>*/ factory: EntityFactory<T>) {
            registerInternal(this, plugin, entityType, factory, -1, priority, hasSpawnEgg)
            //register(plugin, entityType, factory, priority, hasSpawnEgg)
        }
        fun <T: Entity> register(entityType: EntityType<T>, entityFactory: EntityFactory<T>) = register(entityType, factory = entityFactory)
        fun <T: Entity> register(entityType: EntityType<T>, factory: (EntityType<T>, Chunk, CompoundTag) -> T) = register(entityType, EntityFactory(factory))


        register(EntityTypes.PIG, ::SmartPig)
    }
}
