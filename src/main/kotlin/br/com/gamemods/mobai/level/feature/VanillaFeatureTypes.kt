package br.com.gamemods.mobai.level.feature

import cn.nukkit.level.generator.feature.generator.BedrockFeature
import cn.nukkit.level.generator.feature.generator.GroundCoverFeature
import cn.nukkit.utils.Identifier
import kotlin.reflect.KProperty

object VanillaFeatureTypes {
    val SWAMP_HUT = FeatureType("swamp_hut", SwampHutFeature::class.java)
    val PILLAGER_OUTPOST by structure
    val OCEAN_MONUMENT by structure
    val NETHER_BRIDGE by structure

    val BEDROCK = FeatureType("nukkit", "bedrock", BedrockFeature::class.java)
    val GROUND_COVER = FeatureType("nukkit", "ground_cover", GroundCoverFeature::class.java)



    // TODO: Abstract this boilerplate, like IdentifierList
    private inline val structure get() = Type(StructureEntityFeature::class.java)
    private class Type<F: EntityFeature>(private val fClass: Class<F>) {
        var type: FeatureType<F>? = null
        operator fun getValue(thisRef: Any?, property: KProperty<*>): FeatureType<F> {
            type?.let { return it }
            val id: Identifier = Identifier.fromString(property.name.toLowerCase())
            val type = FeatureType(id, fClass)
            this.type = type
            return type
        }
    }
}
