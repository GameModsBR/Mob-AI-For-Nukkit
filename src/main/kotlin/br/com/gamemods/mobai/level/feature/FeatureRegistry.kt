package br.com.gamemods.mobai.level.feature

import br.com.gamemods.mobai.requireNotNull
import cn.nukkit.level.generator.feature.Feature
import cn.nukkit.utils.Identifier

object FeatureRegistry {
    private val registry = mutableMapOf<FeatureType<*>, Feature>()

    operator fun get(id: Identifier): Feature {
        return registry.entries.firstOrNull { it.key.id === id }?.value.requireNotNull {
            "There are no features registered with id $id"
        }
    }

    operator fun <F: Feature> get(type: FeatureType<F>): F {
        return type.featureClass.cast(registry[type].requireNotNull { "The type $type is not registered" })
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <F: Feature> get(feature: F): FeatureType<in F> {
        return registry.entries.firstOrNull { it.value === feature }?.key.requireNotNull {
            "There are no features registered for the feature $feature"
        } as FeatureType<in F>
    }

    operator fun <F: Feature> set(type: FeatureType<F>, feature: F) {
        synchronized(registry) {
            require(type.id !in this) { "The feature ${type.id} is already registered" }
            check(type.featureClass.isInstance(feature)) { "The given feature is not valid for the given type. $type, $feature" }
            registry[type] = feature
        }
    }

    operator fun contains(type: FeatureType<*>) = type in registry
    operator fun contains(type: Identifier) = registry.keys.any { it.id === type }
    operator fun contains(feature: Feature) = registry.values.any { it === feature }
}
