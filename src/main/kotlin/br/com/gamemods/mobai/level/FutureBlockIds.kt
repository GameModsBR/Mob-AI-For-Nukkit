package br.com.gamemods.mobai.level

import cn.nukkit.utils.Identifier
import kotlin.reflect.KProperty

internal object FutureBlockIds {
    val BLUE_FIRE by id
    val NETHER_SPROUTS by id
    val WARPED_ROOTS by id
    val CRIMSON_ROOTS by id
    val WARPED_FUNGUS by id
    val CRIMSON_FUNGUS by id


    // The code bellow allows to define the block ids without having to repeat the property name as parameter
    private inline val id get() = Id()
    private class Id {
        var id: Identifier? = null
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Identifier {
            id?.let { return it }
            val id = Identifier.fromString(property.name.toLowerCase())!!
            this.id = id
            return id
        }
    }
}
