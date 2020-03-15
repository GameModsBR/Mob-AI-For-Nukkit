package br.com.gamemods.mobai.level.feature

import cn.nukkit.level.generator.feature.Feature
import cn.nukkit.level.generator.feature.generator.BedrockFeature
import cn.nukkit.level.generator.feature.generator.GroundCoverFeature

object VanillaFeatureTypes {
    val SWAMP_HUT = feature<SwampHutFeature>("swamp_hut")
    val PILLAGER_OUTPOST = feature<PillagerOutpostFeature>("pillager_outpost")
    val OCEAN_MONUMENT = feature<OceanMonumentFeature>("ocean_monument")
    val NETHER_BRIDGE = feature<NetherFortressFeature>("nether_bridge")

    val BEDROCK = feature<BedrockFeature>("nukkit", "bedrock")
    val GROUND_COVER = feature<GroundCoverFeature>("nukkit", "ground_cover")

    private inline fun <reified F: Feature> feature(name: String) = FeatureType(name, F::class.java)
    private inline fun <reified F: Feature> feature(namespace: String, name: String) = FeatureType(namespace, name, F::class.java)
}
