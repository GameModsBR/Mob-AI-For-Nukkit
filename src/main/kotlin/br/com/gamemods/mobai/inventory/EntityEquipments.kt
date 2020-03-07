package br.com.gamemods.mobai.inventory

import cn.nukkit.entity.impl.BaseEntity
import cn.nukkit.item.Item
import cn.nukkit.network.protocol.MobArmorEquipmentPacket
import cn.nukkit.player.Player
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class EntityEquipments(val holder: BaseEntity): AbstractMutableList<Item>() {
    private val armorSlots = arrayOf(EMPTY_ITEM, EMPTY_ITEM, EMPTY_ITEM, EMPTY_ITEM)
    var helmet by ArmorTracker(0)
    var chestplate by ArmorTracker(1)
    var leggings by ArmorTracker(2)
    var boots by ArmorTracker(3)
    var mainHand by HandTracker(true)
    var offHand by HandTracker(false)

    fun sendArmorContents(players: Iterable<Player> = holder.viewers) {
        val packet = MobArmorEquipmentPacket().apply {
            eid = holder.uniqueId
            slots = Array(armorSlots.size) { armorSlots[it].clone() }
        }

        for (player in players) {
            player.dataPacket(packet)
        }
    }

    private inner class ArmorTracker(private val slot: Int): ReadWriteProperty<Any?, Item> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Item {
            return armorSlots[slot].clone()
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Item) {
            val cloned = value.clone()
            armorSlots[slot] = cloned
            sendArmorContents()
        }
    }

    private inner class HandTracker(main: Boolean): ReadWriteProperty<Any?, Item> {
        private var currentItem = EMPTY_ITEM
        override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Item) {
            currentItem = value.clone()
            //TODO Send the equipment to the viewers
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): Item {
            return currentItem.clone()
        }
    }

    override val size get() = 6

    override fun get(index: Int): Item {
        if (index !in 0 until size) throw IndexOutOfBoundsException(index.toString())
        if (index in 0..3) return armorSlots[index].clone()
        if (index == 4) return mainHand.clone()
        return offHand.clone()
    }

    override fun add(index: Int, element: Item) {
        throw UnsupportedOperationException()
    }

    override fun removeAt(index: Int): Item {
        throw UnsupportedOperationException()
    }

    override fun set(index: Int, element: Item): Item {
        if (index !in 0 until size) throw IndexOutOfBoundsException(index.toString())
        if (index in 0..3) {
            val old = armorSlots[index]
            armorSlots[index] = element.clone()
            sendArmorContents()
            return old
        }
        if (index == 4) {
            val old = mainHand
            mainHand = element
            return old
        }
        val old = offHand
        offHand = element
        return old
    }
}
