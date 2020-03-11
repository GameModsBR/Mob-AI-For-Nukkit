package br.com.gamemods.mobai.entity.smart.logic

import br.com.gamemods.mobai.ExtraAttributeIds
import br.com.gamemods.mobai.entity.*
import br.com.gamemods.mobai.entity.attribute.AttributeModifier
import br.com.gamemods.mobai.entity.attribute.AttributeModifier.Operation.*
import br.com.gamemods.mobai.entity.definition.EntityDefinition
import br.com.gamemods.mobai.level.hasCollision
import br.com.gamemods.mobai.math.clamp
import cn.nukkit.entity.Attribute
import cn.nukkit.entity.data.EntityFlag
import cn.nukkit.level.BlockPosition
import java.util.*

interface InitLogic: PersistenceLogic {
    fun init() {
        initData()
        initAttributes()
        initDefinitions()
        loadNBT()
    }

    fun initDefinitions() {
        definitions += EntityDefinition(entity.type.identifier)
    }

    fun initData() {
        entity.addFlags(EntityFlag.GRAVITY, EntityFlag.CAN_CLIMB)
    }

    fun initAttributes() { entity {
        movementSpeed = 0F
        addAttributes(
            Attribute.KNOCKBACK_RESISTANCE,
            Attribute.MOVEMENT_SPEED,
            ExtraAttributeIds.UNDERWATER_MOVEMENT,
            Attribute.FOLLOW_RANGE,
            Attribute.ABSORPTION
        )
        addAttribute(maxHealthAttribute)
        maxHealthAttribute.baseValue = smart.maxHealth
        attribute(Attribute.ABSORPTION).maxValue = 16F
        attribute(Attribute.MOVEMENT_SPEED).baseValue = 0.25F
    }}

    fun recalculateAttribute(attribute: Attribute): Float {
        val base = attributeModifiers[ADDITION]?.values?.fold(attribute.defaultValue.toDouble()) { current, modifier ->
            current + modifier.amount
        } ?: attribute.defaultValue.toDouble()

        val multiplication = attributeModifiers[MULTIPLY_BASE]?.values?.fold(base) { current, modifier ->
            current + base * modifier.amount
        } ?: base

        val final = attributeModifiers[MULTIPLY_TOTAL]?.values?.fold(multiplication) { current, modifier ->
            current * (1.0 + modifier.amount)
        } ?: multiplication

        return final.toFloat().clamp(attribute.minValue, attribute.maxValue)
    }

    override fun updateAttribute(id: Int): Attribute? {
        val attribute = attributes[id] ?: return null
        attribute.value = recalculateAttribute(attribute)
        if (attribute == maxHealthAttribute) {
            smart.maxHealth = attribute.value
        }
        return attribute
    }

    fun modifyAttribute(id: Int, modifier: AttributeModifier): Attribute? {
        attributeModifiers.getOrPut(modifier.operation, ::mutableMapOf)[modifier.uuid] = modifier
        return updateAttribute(id)
    }

    fun removeAttributeModifier(id: Int, modifier: AttributeModifier): Attribute? {
        attributeModifiers[modifier.operation]?.remove(modifier.uuid)
        return updateAttribute(id)
    }

    fun canSpawn(spawnType: SpawnType): Boolean { base {
        return smart.pathFindingFavor(BlockPosition.from(asVector3i(), level)) >= 0F
    }}

    fun canSpawn(): Boolean { base {
        return !level.hasCollision(this, boundingBox, entities = true, fluids = true)
    }}

    fun postSpawn(spawnType: SpawnType, groupData: Any?, random: Random): Any? {
        modifyAttribute(Attribute.FOLLOW_RANGE, AttributeModifier(
            "Random spawn bonus",
            random.nextGaussian() * 0.05,
            MULTIPLY_BASE
        ))
        //TODO 5% chance to be left handed (is it possible in bedrock?)
        return groupData
    }

    fun spawnsTooManyForEachTry(spawnCount: Int) = false
}
