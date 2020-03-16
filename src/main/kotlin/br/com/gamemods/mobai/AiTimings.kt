package br.com.gamemods.mobai

import co.aikar.timings.TimingsManager

internal object AiTimings {
    private val group = "MobAiFoNukkit"
    val naturalSpawner = TimingsManager.getTiming("$group :: Natural Spawning")
}
