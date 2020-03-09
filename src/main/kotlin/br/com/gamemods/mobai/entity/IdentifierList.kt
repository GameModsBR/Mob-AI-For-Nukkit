package br.com.gamemods.mobai.entity

import cn.nukkit.utils.Identifier
import kotlin.reflect.KProperty

abstract class IdentifierList {
    // The code bellow allows to define the block ids without having to repeat the property name as parameter
    protected inline val id get() = Id()
    protected class Id {
        var id: Identifier? = null
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Identifier {
            id?.let { return it }
            val id = Identifier.fromString(property.name.toLowerCase())!!
            this.id = id
            return id
        }
    }
}
