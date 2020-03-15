package br.com.gamemods.mobai.level.feature

import cn.nukkit.level.Level
import cn.nukkit.math.Vector3i

interface StructureEntityFeature: EntityFeature {
    val name: String

    fun isInsideStructure(level: Level, pos: Vector3i, approximately: Boolean = false): Boolean {
        TODO()
    }
}
