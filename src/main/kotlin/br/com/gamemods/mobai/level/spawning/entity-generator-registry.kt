package br.com.gamemods.mobai.level.spawning

import br.com.gamemods.mobai.level.Dimension
import br.com.gamemods.mobai.level.dimensionType
import cn.nukkit.level.Level
import cn.nukkit.level.generator.Generator
import java.util.*
import kotlin.reflect.KClass

private val entityGeneratorByGenerator = WeakHashMap<Generator, EntityGenerator?>()
private val entityGeneratorByLevel = WeakHashMap<Level, EntityGenerator>()
private val defaultEntityGeneratorByGeneratorClass = mutableMapOf<KClass<out Generator>, EntityGenerator>()
private val defaultEntityGeneratorByDimension = mutableMapOf<Dimension, EntityGenerator>()
object DefaultEntityGenerator: EntityGenerator

var KClass<out Generator>.defaultEntityGenerator: EntityGenerator?
    get() = defaultEntityGeneratorByGeneratorClass[this]
    set(value) {
        if (value == null) defaultEntityGeneratorByGeneratorClass -= this
        else defaultEntityGeneratorByGeneratorClass[this] = value
    }

var Generator.entityGenerator: EntityGenerator?
    get() {
        return if (this in entityGeneratorByGenerator) {
            entityGeneratorByGenerator[this]
        } else {
            javaClass.kotlin.defaultEntityGenerator
        }
    }
    set(value) {
        entityGeneratorByGenerator[this] = value
    }

var Dimension.defaultEntityGenerator: EntityGenerator?
    get() = defaultEntityGeneratorByDimension[this]
    set(value) {
        if (value == null) defaultEntityGeneratorByDimension -= this
        else defaultEntityGeneratorByDimension[this] = value
    }

var Level.entityGenerator
    get() = entityGeneratorByLevel[this]
        ?: generator.entityGenerator
        ?: dimensionType.defaultEntityGenerator
        ?: DefaultEntityGenerator
    set(value) {
        entityGeneratorByLevel[this] = value
    }
