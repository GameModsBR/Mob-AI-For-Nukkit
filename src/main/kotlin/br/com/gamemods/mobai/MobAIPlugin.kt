package br.com.gamemods.mobai

import br.com.gamemods.mobai.ExtraAttributeIds.UNDERWATER_MOVEMENT
import br.com.gamemods.mobai.entity.AttributeRegistry.getIdOrRegister
import br.com.gamemods.mobai.entity.registerEntities
import cn.nukkit.plugin.PluginBase

@Suppress("unused")
class MobAIPlugin: PluginBase() {
    override fun onEnable() {
        registerAttributes()
        registerEntities()
    }

    private fun registerAttributes() {
        UNDERWATER_MOVEMENT = getIdOrRegister("minecraft:underwater_movement", 0F, 340282346638528859811704183484516925440.00f, 0.02F)
    }
}
