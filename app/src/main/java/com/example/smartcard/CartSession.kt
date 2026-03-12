package com.example.smartcard

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf

data class CartItem(
    val barcode: String,
    val name: String,
    val brand: String? = null,
    val price: Double = 0.0,          // TG
    val imageEmoji: String = "🛍️",
    val qty: Int = 1
)

object CartSession {
    val items = mutableStateListOf<CartItem>()
    val lastAddedMessage = mutableStateOf<String?>(null)

    var lastPurchaseId: Int? = null

    fun addOrIncrement(item: CartItem) {
        val idx = items.indexOfFirst { it.barcode == item.barcode }
        if (idx >= 0) {
            val old = items[idx]
            items[idx] = old.copy(qty = old.qty + 1)
        } else {
            items.add(item)
        }
        lastAddedMessage.value = "Added: ${item.name}"
    }

    fun inc(barcode: String) {
        val idx = items.indexOfFirst { it.barcode == barcode }
        if (idx >= 0) items[idx] = items[idx].copy(qty = items[idx].qty + 1)
    }

    fun dec(barcode: String) {
        val idx = items.indexOfFirst { it.barcode == barcode }
        if (idx >= 0) {
            val cur = items[idx]
            val newQty = cur.qty - 1
            if (newQty <= 0) items.removeAt(idx)
            else items[idx] = cur.copy(qty = newQty)
        }
    }

    fun remove(barcode: String) {
        val idx = items.indexOfFirst { it.barcode == barcode }
        if (idx >= 0) items.removeAt(idx)
    }

    fun total(): Double = items.sumOf { it.price * it.qty }
    fun clear() {
        items.clear()
    }
    fun replaceAll(newItems: List<CartItem>) {
        items.clear()
        items.addAll(newItems)
    }
}