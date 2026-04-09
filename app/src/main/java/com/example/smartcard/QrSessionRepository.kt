package com.example.smartcard

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object QrSessionRepository {

    suspend fun confirmSession(traceId: String, sessionId: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                if (user == null) {
                    QrFlowPhoneLog.d(
                        event = "qr_session_confirm_failed",
                        "reason" to "user_not_logged_in",
                        "traceId" to traceId,
                        "sessionId" to sessionId
                    )
                    return@withContext Result.failure(IllegalStateException("User not logged in"))
                }

                val db = FirebaseFirestore.getInstance()
                val sessionRef = db.collection("tabletSessions").document(sessionId)

                Log.d(
                    "QR_FLOW_SESSION",
                    "phone_confirm_start traceId=$traceId sessionId=$sessionId firestorePath=tabletSessions/$sessionId"
                )

                QrFlowPhoneLog.d(
                    event = "qr_session_confirm_start",
                    "traceId" to traceId,
                    "sessionId" to sessionId,
                    "userId" to user.uid,
                    "email" to (user.email ?: "")
                )

                val sessionSnap = Tasks.await(sessionRef.get())
                if (!sessionSnap.exists()) {
                    QrFlowPhoneLog.d(
                        event = "qr_session_not_found",
                        "traceId" to traceId,
                        "sessionId" to sessionId
                    )
                    return@withContext Result.failure(IllegalStateException("Session not found"))
                }

                val cartId = sessionSnap.getString("cartId")?.trim().orEmpty()
                if (cartId.isBlank()) {
                    QrFlowPhoneLog.d(
                        event = "qr_session_invalid",
                        "reason" to "missing_cartId",
                        "traceId" to traceId,
                        "sessionId" to sessionId
                    )
                    return@withContext Result.failure(IllegalStateException("Invalid session: missing cartId"))
                }

                val userName = user.displayName?.takeIf { it.isNotBlank() }
                    ?: user.email?.substringBefore("@")
                    ?: "User"

                val cartRef = db.collection("carts").document(cartId)

                Tasks.await(db.runTransaction { tx ->
                    val freshSession = tx.get(sessionRef)
                    if (!freshSession.exists()) {
                        throw IllegalStateException("Session not found")
                    }

                    val status = freshSession.getString("status") ?: "pending"
                    if (status == "confirmed") {
                        return@runTransaction
                    }

                    tx.update(
                        sessionRef,
                        mapOf(
                            "status" to "confirmed",
                            "confirmedAt" to System.currentTimeMillis(),
                            "confirmedUserId" to user.uid,
                            "confirmedUserEmail" to (user.email ?: ""),
                            "confirmedUserName" to userName
                        )
                    )

                    tx.set(
                        cartRef,
                        mapOf(
                            "cartId" to cartId,
                            "status" to "connected",
                            "sessionStatus" to "connected",
                            "sessionId" to sessionId,
                            "connectedUserId" to user.uid,
                            "connectedUserEmail" to (user.email ?: ""),
                            "connectedUserName" to userName,
                            "updatedAt" to System.currentTimeMillis()
                        ),
                        com.google.firebase.firestore.SetOptions.merge()
                    )
                })

                QrFlowPhoneLog.d(
                    event = "qr_session_confirm_success",
                    "traceId" to traceId,
                    "sessionId" to sessionId,
                    "cartId" to cartId,
                    "userId" to user.uid
                )

                Log.d(
                    "QR_FLOW_SESSION",
                    "phone_confirm_success traceId=$traceId sessionId=$sessionId cartId=$cartId firestorePath=tabletSessions/$sessionId"
                )

                Result.success(cartId)
            } catch (t: Throwable) {
                QrFlowPhoneLog.e(
                    event = "exception",
                    throwable = t,
                    "where" to "confirmSession",
                    "traceId" to traceId,
                    "sessionId" to sessionId
                )
                Result.failure(t)
            }
        }
    }
}
