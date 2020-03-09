package br.com.gamemods.mobai.entity.definition

import cn.nukkit.entity.Entity
import cn.nukkit.entity.data.EntityData
import cn.nukkit.entity.data.EntityData.*
import cn.nukkit.entity.data.EntityFlag
import cn.nukkit.entity.data.EntityFlag.*
import cn.nukkit.nbt.tag.CompoundTag
import java.util.*

object EntityPersistence {
    private val flagNames: Map<EntityFlag, String> = EnumMap(mapOf(
        CHESTED to "Chested",
        ANGRY to "IsAngry",
        BABY to "IsBaby",
        GLIDING to "IsGliding",
        ORPHANED to "IsOrphaned",
        PREGNANT to "IsPregnant",
        SWIMMING to "IsSwimming",
        TAMED to "IsTamed",
        SADDLED to "Saddled",
        SHEARED to "Sheared",
        SHOWBASE to "ShowBottom",
        SITTING to "Sitting"
    ))

    private val dataNames: Map<EntityData, DataType<*, *>> = EnumMap(mapOf(
        COLOR to DataType("Color", ByteSerializer),
        COLOR_2 to DataType("Color2", ByteSerializer),
        FUSE_LENGTH to DataType("Fuse", ByteToIntSerializer),
        AIR to DataType("Air", ShortSerializer),
        LIMITED_LIFE to DataType("limitedLife", IntSerializer),
        MARK_VARIANT to DataType("MarkVariant", IntSerializer),
        SKIN_ID to DataType("SkinID", IntSerializer),
        STRENGTH to DataType("Strength", IntSerializer),
        MAX_STRENGTH to DataType("StrengthMax", IntSerializer),
        VARIANT to DataType("Variant", IntSerializer),
        LEAD_HOLDER_EID to DataType("LeasherID", LongSerializer),
        OWNER_EID to DataType("OwnerNew", LongSerializer),
        TARGET_EID to DataType("TargetID", LongSerializer)
    ))

    fun saveData(entity: Entity, storage: CompoundTag) {
        dataNames.forEach { (data, type) ->
            type.save(entity, storage, data)
        }
    }

    fun loadData(entity: Entity, storage: CompoundTag) {
        dataNames.forEach { (data, type) ->
            type.load(entity, storage, data)
        }
    }

    fun saveFlags(flags: Entity, storage: CompoundTag) {
        flagNames.forEach { (flag, name) ->
            if (flags.getFlag(flag)) {
                storage.putBoolean(name, true)
            } else {
                storage.remove(name)
            }
        }
    }

    fun loadFlags(flags: Entity, storage: CompoundTag) {
        flagNames.forEach { (flag, name) ->
            flags.setFlag(flag, storage.getBoolean(name))
        }
    }

    private class DataType<N: Any, E: Any>(
        val name: String,
        val serializer: DataSerializer<N, E>
    ) {
        fun save(entity: Entity, nbt: CompoundTag, data: EntityData) {
            val e = serializer.entityAccessor.load(entity, data)
            val n = serializer.entityToNbt(e)
            serializer.nbtAccessor.save(nbt, name, n)
        }

        fun load(entity: Entity, nbt: CompoundTag, data: EntityData) {
            val n = serializer.nbtAccessor.load(nbt, name)
            val e = serializer.nbtToEntity(n)
            serializer.entityAccessor.save(entity, data, e)
        }
    }

    private object ByteNbtAccessor: NbtAccessor<Int>(CompoundTag::getByte, CompoundTag::putByte)

    private object ShortNbtAccessor: NbtAccessor<Int>(CompoundTag::getShort, CompoundTag::putShort)

    private object IntNbtAccessor: NbtAccessor<Int>(CompoundTag::getInt, CompoundTag::putInt)

    private object LongNbtAccessor: NbtAccessor<Long>(CompoundTag::getLong, CompoundTag::putLong)

    private object ByteEntityAccessor: EntityAccessor<Int>(
        { e, d -> e.getByteData(d).toInt() },
        Entity::setByteData
    )

    private object ShortEntityAccessor: EntityAccessor<Int>(
        { e, d -> e.getShortData(d).toInt() },
        Entity::setShortData
    )

    private object IntEntityAccessor: EntityAccessor<Int>(Entity::getIntData, Entity::setIntData)

    private object LongEntityAccessor: EntityAccessor<Long>(Entity::getLongData, Entity::setLongData)

    private object ByteSerializer: RedundantDataSerializer<Int>(
        ByteNbtAccessor, ByteEntityAccessor
    )

    private object ShortSerializer: RedundantDataSerializer<Int>(
        ShortNbtAccessor, ShortEntityAccessor
    )

    private object IntSerializer: RedundantDataSerializer<Int>(
        IntNbtAccessor, IntEntityAccessor
    )

    private object LongSerializer: RedundantDataSerializer<Long>(
        LongNbtAccessor, LongEntityAccessor
    )

    private object ByteToIntSerializer: RedundantDataSerializer<Int>(
        ByteNbtAccessor, IntEntityAccessor
    )

    private open class RedundantDataSerializer<T: Any>(
        nbtAccessor: NbtAccessor<T>,
        entityAccessor: EntityAccessor<T>
    ): DataSerializer<T, T>(nbtAccessor, entityAccessor, { it }, { it })

    private open class DataSerializer<N: Any, E: Any>(
        val nbtAccessor: NbtAccessor<N>,
        val entityAccessor: EntityAccessor<E>,
        val nbtToEntity: (N) -> E,
        val entityToNbt: (E) -> N
    )

    private open class NbtAccessor<T: Any>(
        val load: (CompoundTag, String) -> T,
        val save: (CompoundTag, String, T) -> CompoundTag
    )
    private open class EntityAccessor<T: Any>(
        val load: (Entity, EntityData) -> T,
        val save: (Entity, EntityData, T) -> Unit
    )
}
