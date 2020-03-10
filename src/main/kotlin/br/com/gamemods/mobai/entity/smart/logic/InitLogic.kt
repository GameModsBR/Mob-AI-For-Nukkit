package br.com.gamemods.mobai.entity.smart.logic

import br.com.gamemods.mobai.ExtraAttributeIds
import br.com.gamemods.mobai.entity.addFlags
import br.com.gamemods.mobai.entity.attribute
import br.com.gamemods.mobai.entity.baseValue
import br.com.gamemods.mobai.entity.definition.EntityDefinition
import br.com.gamemods.mobai.entity.movementSpeed
import cn.nukkit.entity.Attribute
import cn.nukkit.entity.data.EntityFlag

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
        addAttribute(healthAttribute)
        attribute(Attribute.ABSORPTION).maxValue = 16F
        attribute(Attribute.MOVEMENT_SPEED).baseValue = 0.25F
    }}
}
