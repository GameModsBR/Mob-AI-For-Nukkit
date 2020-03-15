package br.com.gamemods.mobai.level.feature

import br.com.gamemods.mobai.cast
import br.com.gamemods.mobai.orThrow
import cn.nukkit.level.generator.feature.Feature
import cn.nukkit.registry.Registry
import cn.nukkit.registry.RegistryException
import cn.nukkit.utils.Identifier

object FeatureRegistry: Registry {
    private val registry = mutableMapOf<FeatureType<*>, Feature>()
    private var closed = false

    operator fun get(id: Identifier): Feature {
        return registry.entries.firstOrNull { it.key.id === id }?.value
            ?: throw RegistryException("There are no features registered with id $id")
    }

    operator fun <F: Feature> get(type: FeatureType<F>): F {
        return type.featureClass.cast(registry[type] ?: throw RegistryException("The type $type is not registered"))
    }

    operator fun <F: Feature> get(feature: F): FeatureType<in F> {
        return registry.entries.firstOrNull { it.value === feature }?.key.orThrow {
            RegistryException("There are no features registered for the feature $feature")
        }.cast()
    }

    operator fun <F: Feature> set(type: FeatureType<F>, feature: F) {
        synchronized(registry) {
            if (closed) throw RegistryException("Registration is closed")
            check(type.featureClass.isInstance(feature)) { "The given feature is not valid for the given type. $type, $feature" }
            registry.keys.firstOrNull { it.id == type.id }?.let { registered ->
                require(registered == type) { "${type.id} is already registered with a different class: ${registered.featureClass}. Tried to register with ${type.featureClass}" }
            }
            registry[type] = feature
        }
    }

    override fun close() {
        synchronized(registry) {
            if (closed) throw RegistryException("Registration is closed")
            closed = true
        }
    }

    operator fun contains(type: FeatureType<*>) = type in registry
    operator fun contains(type: Identifier) = registry.keys.any { it.id === type }
    operator fun contains(feature: Feature) = registry.values.any { it === feature }
}
