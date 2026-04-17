package com.example.smartcard

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

object RemoteCartRepository {

    private val db = FirebaseFirestore.getInstance()

    fun listenToCart(
        cartId: String,
        onCartChanged: (List<CartItem>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration {
        return db.collection("carts")
            .document(cartId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.message ?: "Failed to listen cart")
                    return@addSnapshotListener
                }

                if (snapshot == null || !snapshot.exists()) {
                    onCartChanged(emptyList())
                    return@addSnapshotListener
                }

                @Suppress("UNCHECKED_CAST")
                val items = snapshot.get("items") as? List<Map<String, Any>> ?: emptyList()

                val cartItems = items.map { item ->
                    CartItem(
                        barcode = item["barcode"] as? String ?: "",
                        name = item["name"] as? String ?: "",
                        brand = item["brand"] as? String ?: "",
                        price = ((item["price"] as? Number)?.toInt() ?: 0).toDouble(),
                        imageEmoji = item["imageEmoji"] as? String ?: "🛍️",
                        imageUrl = item["imageUrl"] as? String ?: "",
                        qty = (item["quantity"] as? Number)?.toInt() ?: 1
                    )
                }

                onCartChanged(cartItems)
            }
    }

    fun updateCartItems(
        cartId: String,
        items: List<CartItem>,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val firestoreItems = items.map { item ->
            hashMapOf(
                "name" to item.name,
                "brand" to item.brand,
                "barcode" to item.barcode,
                "price" to item.price,
                "quantity" to item.qty,
                "imageEmoji" to item.imageEmoji,
                "imageUrl" to item.imageUrl
            )
        }

        val totalAmount = items.sumOf { it.price * it.qty }

        val updateData = mapOf(
            "items" to firestoreItems,
            "totalAmount" to totalAmount
        )

        db.collection("carts")
            .document(cartId)
            .update(updateData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to update cart") }
    }

    fun increaseQty(cartId: String, currentItems: List<CartItem>, barcode: String) {
        val updated = currentItems.map {
            if (it.barcode == barcode) it.copy(qty = it.qty + 1) else it
        }
        updateCartItems(cartId, updated)
    }

    fun decreaseQty(cartId: String, currentItems: List<CartItem>, barcode: String) {
        val updated = currentItems.mapNotNull {
            if (it.barcode == barcode) {
                val newQty = it.qty - 1
                if (newQty <= 0) null else it.copy(qty = newQty)
            } else {
                it
            }
        }
        updateCartItems(cartId, updated)
    }

    fun removeItem(cartId: String, currentItems: List<CartItem>, barcode: String) {
        val updated = currentItems.filterNot { it.barcode == barcode }
        updateCartItems(cartId, updated)
    }

    fun clearCart(cartId: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        db.collection("carts")
            .document(cartId)
            .update(
                mapOf(
                    "items" to emptyList<Map<String, Any>>(),
                    "totalAmount" to 0
                )
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to clear cart") }
    }

    fun addProductToRemoteCart(
        cartId: String,
        product: Product,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val cartRef = db.collection("carts").document(cartId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(cartRef)

            @Suppress("UNCHECKED_CAST")
            val items = snapshot.get("items") as? List<Map<String, Any>> ?: emptyList()

            val mutableItems = items.map { it.toMutableMap() }.toMutableList()

            val existingIndex = mutableItems.indexOfFirst {
                (it["barcode"] as? String) == product.barcode
            }

            if (existingIndex >= 0) {
                val existing = mutableItems[existingIndex]
                val oldQty = (existing["quantity"] as? Number)?.toInt() ?: 0
                existing["quantity"] = oldQty + 1
                mutableItems[existingIndex] = existing
            } else {
                mutableItems.add(
                    mutableMapOf(
                        "name" to product.name,
                        "brand" to product.brand,
                        "barcode" to product.barcode,
                        "price" to product.price,
                        "quantity" to 1,
                        "imageUrl" to "",
                        "imageEmoji" to when (product.barcode) {
                            "1234567890123" -> "🥛"
                            "1234567890179" -> "🧼"
                            "1234567890155" -> "🥔"
                            else -> "🛍️"
                        }
                    )
                )
            }

            val totalAmount = mutableItems.sumOf { item ->
                val price = (item["price"] as? Number)?.toInt() ?: 0
                val qty = (item["quantity"] as? Number)?.toInt() ?: 0
                price * qty
            }

            transaction.update(
                cartRef,
                mapOf(
                    "items" to mutableItems,
                    "totalAmount" to totalAmount
                )
            )
        }
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to add product to cart")
            }
    }
}