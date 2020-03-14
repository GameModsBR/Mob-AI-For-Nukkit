package br.com.gamemods.mobai.entity

import br.com.gamemods.mobai.entity.monster.Piglin
import br.com.gamemods.mobai.entity.passive.Fox
import cn.nukkit.entity.Entity
import cn.nukkit.entity.EntityType
import cn.nukkit.entity.EntityTypes

object FutureEntityTypes {
    val FOX = type<Fox>("fox")
    val ZOMBIFIED_PIGLIN = EntityTypes.ZOMBIE_PIGMAN
    val PIGLIN = type<Piglin>("piglin")

    private inline fun <reified T: Entity> type(id: String): EntityType<T> = EntityType.from(id, T::class.java)
}
