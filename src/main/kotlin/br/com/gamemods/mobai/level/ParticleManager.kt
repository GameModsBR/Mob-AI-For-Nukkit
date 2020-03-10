package br.com.gamemods.mobai.level

import br.com.gamemods.mobai.entity.smart.SmartEntity
import br.com.gamemods.mobai.entity.smart.logic.entity
import cn.nukkit.level.particle.HeartParticle

object ParticleManager {
    //status 18
    fun createHearts(smart: SmartEntity, count: Int = 7) {
        val level = smart.entity.level
        for (i in 1..count) {
            level.addParticle(HeartParticle(smart.randomParticlePos(yOffset = 0.5)))
        }
    }
}
