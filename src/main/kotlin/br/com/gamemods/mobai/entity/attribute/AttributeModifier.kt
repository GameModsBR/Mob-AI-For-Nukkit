package br.com.gamemods.mobai.entity.attribute

import java.util.*

data class AttributeModifier(
    val name: String,
    val amount: Double,
    val operation: Operation,
    val uuid: UUID = UUID.randomUUID()
) {
    var serialize: Boolean = true
    enum class Operation {
        ADDITION,
        MULTIPLY_BASE,
        MULTIPLY_TOTAL
        ;
        companion object {
            private val VALUES = values()
            operator fun get(id: Int) = requireNotNull(VALUES.getOrNull(id)) { "Invalid operation id: $id" }
        }
    }
}
