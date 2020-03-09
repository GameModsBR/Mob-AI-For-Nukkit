package br.com.gamemods.mobai.entity.definition

import br.com.gamemods.mobai.entity.definition.DefinitionOperation.ADD
import br.com.gamemods.mobai.entity.definition.DefinitionOperation.REMOVE
import cn.nukkit.utils.Identifier

class EntityDefinitionCollection(baseDefinitions: Collection<EntityDefinition>): AbstractMutableCollection<EntityDefinition>() {
    constructor(): this(emptySet())
    private val custom = mutableSetOf<EntityDefinition>()
    private val base = baseDefinitions.asSequence()
        .filter { it.operation == ADD }
        .toList()

    override var size = base.size
        private set

    override fun iterator(): MutableIterator<EntityDefinition> {
        return DefinitionIterator()
    }

    fun identifierIterator(): MutableIterator<Identifier> {
        return IdentifierIterator()
    }

    override fun clear() {
        custom.clear()
        custom.addAll(base.map { it.opposite() })
    }

    fun reset() {
        custom.clear()
    }

    fun add(id: Identifier) = add(id.toDefinition())
    fun remove(id: Identifier) = add(id.toDefinition(REMOVE))
    operator fun contains(id: Identifier) = contains(id.toDefinition())

    operator fun plusAssign(id: Identifier) { add(id) }
    operator fun minusAssign(id: Identifier) { remove(id) }


    override fun add(element: EntityDefinition): Boolean {
        return when (element.operation) {
            ADD -> {
                if (element in base) {
                    custom.remove(element.opposite())
                } else {
                    custom.add(element)
                }.also {
                    if (it) {
                        size++
                    }
                }
            }
            REMOVE -> {
                val add = element.opposite()
                if (add in base) {
                    custom.add(element)
                } else {
                    custom.remove(add)
                }.also {
                    if (it) {
                        size--
                    }
                }
            }
        }
    }

    override fun contains(element: EntityDefinition): Boolean {
        return if (element.operation == REMOVE) {
            element in custom
        } else {
            element in custom || element in base && element.opposite() !in custom
        }
    }

    override fun remove(element: EntityDefinition): Boolean {
        return add(element.opposite())
    }

    private fun Identifier.toDefinition(operation: DefinitionOperation = ADD) = EntityDefinition(this, operation)

    private fun createSequence() = sequenceOf (
        base.asSequence().filter { it.opposite() !in custom },
        custom.asSequence().filter { it.operation == ADD }
    ).flatMap { it }

    private inner class DefinitionIterator: MutableIterator<EntityDefinition> {
        private val iter = createSequence().iterator()

        private var last: EntityDefinition? = null

        override fun hasNext(): Boolean {
            return iter.hasNext()
        }

        override fun next(): EntityDefinition {
            return iter.next().also {
                last = it
            }
        }

        override fun remove() {
            remove(checkNotNull(last))
        }
    }

    private inner class IdentifierIterator: MutableIterator<Identifier> {
        private val iter = createSequence().map { Identifier.from(it.namespace, it.name) }.iterator()

        private var last: Identifier? = null

        override fun hasNext(): Boolean {
            return iter.hasNext()
        }

        override fun next(): Identifier {
            return iter.next().also {
                last = it
            }
        }

        override fun remove() {
            remove(checkNotNull(last))
        }
    }
}
