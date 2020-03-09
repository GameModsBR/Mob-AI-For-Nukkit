package br.com.gamemods.mobai.entity

import cn.nukkit.entity.Attribute
import cn.nukkit.utils.ServerException

object AttributeRegistry {
    private val freeAttributeIds = generateSequence(1000) { it + 1 }
        .filter {
            try { Attribute.getAttribute(it); false }
            catch (e: ServerException) { true }
        }.iterator()

    fun getOrRegister(name: String, minValue: Float, maxValue: Float, defaultValue: Float, shouldSend: Boolean = true): Attribute {
        Attribute.getAttributeByName(name)?.let { return it }
        Attribute.addAttribute(freeAttributeIds.next(), name, minValue, maxValue, defaultValue, shouldSend)
        return checkNotNull(Attribute.getAttributeByName(name)) {
            "The attribute registration of $name failed"
        }
    }

    fun getIdOrRegister(name: String, minValue: Float, maxValue: Float, defaultValue: Float, shouldSend: Boolean = true): Int {
        return getOrRegister(name, minValue, maxValue, defaultValue, shouldSend).id
    }

    fun load(name: String, minValue: Float, maxValue: Float, defaultValue: Float, currentValue: Float, shouldSend: Boolean = true): Attribute {
        return getOrRegister(name, minValue, maxValue, defaultValue, shouldSend).also {
            it.minValue = minValue
            it.maxValue = maxValue
            it.defaultValue = defaultValue
            it.value = currentValue
        }
    }
}
