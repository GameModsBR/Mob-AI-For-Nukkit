package br.com.gamemods.mobai.entity.smart.logic

import br.com.gamemods.mobai.entity.AttributeRegistry
import br.com.gamemods.mobai.entity.definition.EntityDefinition
import br.com.gamemods.mobai.entity.definition.EntityPersistence
import br.com.gamemods.mobai.nbt.forEach
import br.com.gamemods.mobai.nbt.listen
import cn.nukkit.nbt.NBTIO
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.ListTag
import cn.nukkit.nbt.tag.StringTag

interface PersistenceLogic: SplitLogic {
    val equipments get() = ai.equipments

    fun saveNBT() {
        val nbt = base.namedTag
        saveEquipments(nbt)
        saveAttributes(nbt)
        saveDefinitions(nbt)
        saveCommonData(nbt)
        saveSpecificData(nbt)
    }

    fun loadNBT() {
        val nbt = base.namedTag
        loadEquipments(nbt)
        loadAttributes(nbt)
        loadDefinitions(nbt)
        loadCommonData(nbt)
        loadSpecificData(nbt)
    }

    fun loadSpecificData(nbt: CompoundTag) {
    }

    fun saveSpecificData(nbt: CompoundTag) {
    }

    fun saveAttributes(nbt: CompoundTag) {
        ListTag<CompoundTag>("Attributes").apply {
            attributes.values.forEach { attribute ->
                add(
                    CompoundTag()
                        .putString("Name", attribute.name)
                        .putFloat("Base", attribute.defaultValue)
                        .putFloat("Current", attribute.value)
                        .putFloat("Max", attribute.maxValue)
                        .putFloat("Min", attribute.minValue)
                )
            }
            nbt.putList(this)
        }
    }

    fun loadAttributes(nbt: CompoundTag) {
        nbt.listenList<CompoundTag>("Attributes") { list ->
            list.forEach {
                val name = it.getString("Name").takeIf { n-> n.isNotBlank() } ?: return@forEach
                val base = it.getFloat("Base")
                val current = it.getFloat("Current")
                val max = it.getFloat("Max")
                val min = it.getFloat("Min")
                addAttribute(AttributeRegistry.load(name, min, max, base, current))
            }
        }
    }

    fun saveEquipments(nbt: CompoundTag) {
        ListTag<CompoundTag>("Armor").apply {
            add(NBTIO.putItemHelper(equipments.helmet))
            add(NBTIO.putItemHelper(equipments.chestplate))
            add(NBTIO.putItemHelper(equipments.leggings))
            add(NBTIO.putItemHelper(equipments.boots))
            nbt.putList(this)
        }
        ListTag<CompoundTag>("Mainhand").apply {
            add(NBTIO.putItemHelper(equipments.mainHand))
            nbt.putList(this)
        }
        ListTag<CompoundTag>("Offhand").apply {
            add(NBTIO.putItemHelper(equipments.offHand))
            nbt.putList(this)
        }
    }

    fun loadEquipments(nbt: CompoundTag) {
        equipments.clear()
        nbt.listenList<CompoundTag>("Armor") { inv ->
            inv.listen(0) { equipments.helmet = NBTIO.getItemHelper(it) }
            inv.listen(1) { equipments.chestplate = NBTIO.getItemHelper(it) }
            inv.listen(2) { equipments.leggings = NBTIO.getItemHelper(it) }
            inv.listen(3) { equipments.boots = NBTIO.getItemHelper(it) }
        }
        nbt.listenList<CompoundTag>("Mainhand") { inv ->
            inv.listen(0) { equipments.mainHand = NBTIO.getItemHelper(it) }
        }
        nbt.listenList<CompoundTag>("Offhand") { inv ->
            inv.listen(0) { equipments.offHand = NBTIO.getItemHelper(it) }
        }
    }

    fun saveDefinitions(nbt: CompoundTag) {
        ListTag<StringTag>("definitions").apply {
            definitions.forEach {
                add(StringTag("", it.toString()))
            }
            nbt.putList(this)
        }
    }

    fun loadDefinitions(nbt: CompoundTag) {
        definitions.reset()
        nbt.listenList<StringTag>("definitions") { list ->
            list.forEach {
                definitions += EntityDefinition(it.data)
            }
        }
    }

    fun saveCommonData(nbt: CompoundTag) {
        EntityPersistence.saveFlags(entity, nbt)
        EntityPersistence.saveData(entity, nbt)
        nbt.putBoolean("Persistent", isPersistent)
        if (this is Breedable && isBreedable) {
            nbt.putInt("InLove", loveTicks)
            nbt.putInt("BreedCooldown", breedingAge)
            nbt.putInt("ForcedBreedingAge", forcedBreedingAge)
        }
    }

    fun loadCommonData(nbt: CompoundTag) {
        EntityPersistence.loadFlags(entity, nbt)
        EntityPersistence.loadData(entity, nbt)
        nbt.listenBoolean("Persistent") { isPersistent = it }
        if (this is Breedable && isBreedable) {
            nbt.listenInt("InLove") { loveTicks = it }
            nbt.listenInt("BreedCooldown") { breedingAge = it }
            nbt.listenInt("ForcedBreedingAge") { forcedBreedingAge = it }
        }
    }
}
