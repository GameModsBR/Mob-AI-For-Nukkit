package br.com.gamemods.mobai.level.feature

import br.com.gamemods.mobai.MobAIPlugin
import cn.nukkit.level.generator.feature.generator.BedrockFeature
import cn.nukkit.level.generator.feature.generator.GroundCoverFeature

internal fun MobAIPlugin.registerFeatures() {
    logger.debug("Registering features")
    with(VanillaFeatureTypes) {
        with(FeatureRegistry) {
            this[SWAMP_HUT] = SwampHutFeature()
            this[PILLAGER_OUTPOST] = PillagerOutpostFeature()
            this[OCEAN_MONUMENT] = OceanMonumentFeature()
            this[NETHER_BRIDGE] = NetherFortressFeature()

            this[BEDROCK] = BedrockFeature.INSTANCE
            this[GROUND_COVER] = GroundCoverFeature.INSTANCE
        }
    }
}
