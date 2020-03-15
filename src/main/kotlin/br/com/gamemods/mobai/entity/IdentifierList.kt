package br.com.gamemods.mobai.entity

import cn.nukkit.utils.Identifier
import kotlin.reflect.KProperty

abstract class IdentifierList(val namespace: String = "minecraft") {
    // The code bellow allows to define the block ids without having to repeat the property name as parameter
    protected inline val id get() = Id()
    protected inner class Id {
        var id: Identifier? = null
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Identifier {
            id?.let { return it }
            val id: Identifier = Identifier.from(namespace, property.name.toLowerCase())
            this.id = id
            return id
        }
    }
}
