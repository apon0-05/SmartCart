package com.example.smartcard

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object PurchaseRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun savePurchase(
        cartItems: List<CartItem>,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            onError("User is not logged in")
            return
        }

        val uid = currentUser.uid
        val email = currentUser.email ?: ""

        val receiptId = "RCP_" + UUID.randomUUID()
            .toString()
            .replace("-", "")
            .take(8)
            .uppercase()

        val purchaseTime = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.getDefault()
        ).format(Date())

        val itemsList = cartItems.map { item ->
            hashMapOf(
                "name" to item.name,
                "brand" to item.brand,
                "barcode" to item.barcode,
                "price" to item.price,
                "quantity" to item.qty,
                "imageEmoji" to item.imageEmoji
            )
        }

        val totalItems = cartItems.sumOf { it.qty }
        val totalAmount = cartItems.sumOf { it.price.toDouble() * it.qty }

        val userData = hashMapOf(
            "uid" to uid,
            "email" to email,
            "createdAt" to purchaseTime
        )

        val purchaseData = hashMapOf(
            "receiptId" to receiptId,
            "purchaseTime" to purchaseTime,
            "totalAmount" to totalAmount,
            "totalItems" to totalItems,
            "items" to itemsList
        )

        db.collection("users")
            .document(uid)
            .set(userData)
            .addOnSuccessListener {
                db.collection("users")
                    .document(uid)
                    .collection("purchases")
                    .document(receiptId)
                    .set(purchaseData)
                    .addOnSuccessListener {
                        onSuccess(receiptId)
                    }
                    .addOnFailureListener { e ->
                        onError(e.message ?: "Failed to save purchase")
                    }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to save user")
            }
    }
}