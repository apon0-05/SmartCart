package com.example.smartcard

import android.util.Log
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
        if (ScanPayloadClassifier.classify(cartId) == ScanPayloadType.PRODUCT_BARCODE) {
            QrFlowPhoneLog.d(
                event = "cart_connect_rejected_product_like",
                "cartId" to cartId
            )
            onError("Use product scanner for this barcode")
            return
        }

        val user = auth.currentUser
        if (user == null) {
            QrFlowPhoneLog.d(
                event = "cart_connect_failed",
                "reason" to "user_not_logged_in",
                "cartId" to cartId
            )
            onError("User not logged in")
            return
        }

        QrFlowPhoneLog.d(
            event = "cart_connect_start",
            "cartId" to cartId,
            "userId" to user.uid,
            "email" to (user.email ?: "")
        )

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
                    QrFlowPhoneLog.d(
                        event = "cart_not_found",
                        "cartId" to cartId,
                        "userId" to user.uid
                    )
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
                        CartConnectionSession.updateConnection(cartId)
                        Log.d(SmartCartLogTags.CART, "connect_success cartId=$cartId userId=${user.uid}")
                        QrFlowPhoneLog.d(
                            event = "cart_connect_success",
                            "cartId" to cartId,
                            "userId" to user.uid
                        )
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        Log.e(SmartCartLogTags.CART, "connect_update_failed cartId=$cartId userId=${user.uid}", e)
                        QrFlowPhoneLog.e(
                            event = "exception",
                            throwable = e,
                            "where" to "cart_update",
                            "cartId" to cartId,
                            "userId" to user.uid
                        )
                        onError(e.message ?: "Failed to connect cart")
                    }
            }
            .addOnFailureListener { e ->
                Log.e(SmartCartLogTags.CART, "connect_load_failed cartId=$cartId userId=${user.uid}", e)
                QrFlowPhoneLog.e(
                    event = "exception",
                    throwable = e,
                    "where" to "cart_load",
                    "cartId" to cartId,
                    "userId" to (user.uid)
                )
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
                CartConnectionSession.updateConnection(null)
                Log.d(SmartCartLogTags.CART, "disconnect_success cartId=$cartId")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(SmartCartLogTags.CART, "disconnect_failed cartId=$cartId", e)
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
                    CartConnectionSession.updateConnection(null)
                    Log.d(SmartCartLogTags.CART, "disconnect_current_no_connected_cart userId=${user.uid}")
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
                        CartConnectionSession.updateConnection(null)
                        Log.d(SmartCartLogTags.CART, "disconnect_current_success cartId=${doc.id} userId=${user.uid}")
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        Log.e(SmartCartLogTags.CART, "disconnect_current_failed cartId=${doc.id} userId=${user.uid}", e)
                        onError(e.message ?: "Failed to disconnect cart")
                    }
            }
            .addOnFailureListener { e ->
                Log.e(SmartCartLogTags.CART, "disconnect_current_query_failed userId=${user.uid}", e)
                onError(e.message ?: "Failed to find connected cart")
            }
    }
}