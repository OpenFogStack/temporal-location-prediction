package me.mbe.prp.network.backend

import me.mbe.prp.core.Capacity
import java.time.Instant


class BackendItemWithAnnotation(val item: BackendItem, var noTransfers: Int, val availableFrom: Instant) {

    fun noTransfersInc() {
        noTransfers++
    }

    fun noTransfersDec() {
        noTransfers--
    }

}

class BackendNode(
    val capacity: Capacity,
    val name: String,
    val fullyAvailableItems: MutableMap<String, BackendItemWithAnnotation>,
    var reservedCapacity: Capacity = 0,
) {

    val freeCapacity: Capacity
        get() = capacity - reservedCapacity - fullyAvailableItems.entries.sumOf { it.value.item.size }

    fun deleteItem(itemName: String) {
        if (!fullyAvailableItems.containsKey(itemName)) {
            return
        }
        if (fullyAvailableItems[itemName]!!.noTransfers > 0) {
            throw IllegalArgumentException("item cannot be deleted: transfer in progress")
        }
        fullyAvailableItems.remove(itemName)
    }
}