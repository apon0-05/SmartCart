package com.example.smartcard

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CartConnectionRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun connectToCart(
        cartId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            onError("User not logged in")
            return
        }

        val userName = user.displayName?.takeIf { it.isNotBlank() }
            ?: user.email?.substringBefore("@")
            ?: "User"

        val connectedAt = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.getDefault()
        ).format(Date())

        val cartRef = db.collection("carts").document(cartId)

        cartRef.get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    onError("Cart not found")
                    return@addOnSuccessListener
                }

                val status = doc.getString("status") ?: "available"
                val connectedUserId = doc.getString("connectedUserId") ?: ""

                if (status == "connected" &&
                    connectedUserId.isNotBlank() &&
                    connectedUserId != user.uid
                ) {
                    onError("This cart is already connected to another user")
                    return@addOnSuccessListener
                }

                val updateData = mapOf(
                    "cartId" to cartId,
                    "status" to "connected",
                    "connectedUserId" to user.uid,
                    "connectedUserEmail" to (user.email ?: ""),
                    "connectedUserName" to userName,
                    "connectedAt" to connectedAt
                )

                cartRef.update(updateData)
                    .addOnSuccessListener {
                        CartConnectionSession.connectedCartId = cartId
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        onError(e.message ?: "Failed to connect cart")
                    }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to load cart")
            }
    }

    fun disconnectCart(
        cartId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val clearData = mapOf(
            "status" to "available",
            "connectedUserId" to "",
            "connectedUserEmail" to "",
            "connectedUserName" to "",
            "connectedAt" to ""
        )

        db.collection("carts")
            .document(cartId)
            .update(clearData)
            .addOnSuccessListener {
                CartConnectionSession.connectedCartId = null
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to disconnect cart")
            }
    }

    fun disconnectCurrentUserCart(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            onSuccess()
            return
        }

        db.collection("carts")
            .whereEqualTo("connectedUserId", user.uid)
            .whereEqualTo("status", "connected")
            .get()
            .addOnSuccessListener { result ->
                val doc = result.documents.firstOrNull()

                if (doc == null) {
                    CartConnectionSession.connectedCartId = null
                    onSuccess()
                    return@addOnSuccessListener
                }

                val clearData = mapOf(
                    "status" to "available",
                    "connectedUserId" to "",
                    "connectedUserEmail" to "",
                    "connectedUserName" to "",
                    "connectedAt" to ""
                )

                db.collection("carts")
                    .document(doc.id)
                    .update(clearData)
                    .addOnSuccessListener {
                        CartConnectionSession.connectedCartId = null
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        onError(e.message ?: "Failed to disconnect cart")
                    }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to find connected cart")
            }
    }
}