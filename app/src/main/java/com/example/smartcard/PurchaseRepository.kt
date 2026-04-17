package com.example.smartcard

import android.util.Log
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
        Log.d(SmartCartLogTags.PAYMENT, "payment_start items=${cartItems.size}")
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Log.e(SmartCartLogTags.PAYMENT, "payment_failed reason=user_not_logged_in")
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
                "imageUrl" to item.imageUrl,
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
                        SmartCartUiCache.setPurchaseCount(
                            uid,
                            (SmartCartUiCache.getPurchaseCount(uid) ?: 0) + 1
                        )
                        Log.d(SmartCartLogTags.PAYMENT, "payment_success uid=$uid receiptId=$receiptId amount=$totalAmount")
                        onSuccess(receiptId)
                    }
                    .addOnFailureListener { e ->
                        Log.e(SmartCartLogTags.PAYMENT, "payment_failed uid=$uid reason=save_purchase_failed", e)
                        onError(e.message ?: "Failed to save purchase")
                    }
            }
            .addOnFailureListener { e ->
                Log.e(SmartCartLogTags.PAYMENT, "payment_failed uid=$uid reason=save_user_failed", e)
                onError(e.message ?: "Failed to save user")
            }
    }
}