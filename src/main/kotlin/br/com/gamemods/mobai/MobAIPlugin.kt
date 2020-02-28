package br.com.gamemods.mobai

import br.com.gamemods.mobai.entity.registerEntities
import cn.nukkit.plugin.PluginBase

@Suppress("unused")
class MobAIPlugin: PluginBase() {
    override fun onEnable() {
        registerEntities()
    }
}
