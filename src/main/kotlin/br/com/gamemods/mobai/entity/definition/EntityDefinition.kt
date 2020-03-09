package br.com.gamemods.mobai.entity.definition

import cn.nukkit.utils.Identifier

data class EntityDefinition(
    val name: String,
    val namespace: String,
    val operation: DefinitionOperation = DefinitionOperation.ADD
) {
    private constructor(parts: List<String>): this(
        namespace = if (parts.size == 1)  "minecraft" else parts.first().removePrefix("+").removePrefix("-"),
        name = parts.getOrNull(1) ?: parts.first().removePrefix("+").removePrefix("-"),
        operation = when (parts.first().first()) {
            '+' -> DefinitionOperation.ADD
            '-' -> DefinitionOperation.REMOVE
            else -> DefinitionOperation.ADD
        }
    )
    constructor(serialized: String): this(serialized.split(':', limit = 2))
    constructor(identifier: Identifier, operation: DefinitionOperation = DefinitionOperation.ADD): this(
        name = identifier.name,
        namespace = identifier.namespace,
        operation = operation
    )

    fun opposite() = copy(operation = operation.opposite)

    infix fun eq(identifier: Identifier) = name == identifier.name && namespace == identifier.namespace

    override fun toString(): String {
        return "${operation.char}$namespace:$name"
    }
}
