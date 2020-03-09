package br.com.gamemods.mobai.entity.definition

enum class DefinitionOperation(val char: Char) {
    ADD('+'),
    REMOVE('-')
    ;
    val opposite get() = when (this) {
        ADD -> REMOVE
        REMOVE -> ADD
    }
}
