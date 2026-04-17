package com.example.smartcard

import com.example.smartcard.local.LocalSessionClient
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object QrSessionRepository {

    private fun normalizeStatus(raw: String?): String = raw?.trim()?.lowercase().orEmpty()

    /**
     * Confirm a session via the tablet's embedded local server (no Firebase).
     * [tabletBaseUrl] e.g. "http://192.168.1.42:8080"
     */
    suspend fun confirmLocalSession(
        traceId: String,
        sessionId: String,
        tabletBaseUrl: String,
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            val user = FirebaseAuth.getInstance().currentUser
            val userId = user?.uid ?: "phone_user_${System.currentTimeMillis()}"
            val userName = user?.displayName?.takeIf { it.isNotBlank() }
                ?: user?.email?.substringBefore("@")
                ?: "Phone User"
            val userEmail = user?.email ?: ""

            Log.d("LOCAL_CLIENT", "confirm_local_start traceId=$traceId sessionId=$sessionId url=$tabletBaseUrl")
            Log.d(SmartCartLogTags.QR, "confirm_local_start traceId=$traceId sessionId=$sessionId")
            QrFlowPhoneLog.d(
                event = "qr_local_confirm_start",
                "traceId" to traceId,
                "sessionId" to sessionId,
                "tabletBaseUrl" to tabletBaseUrl,
                "userId" to userId
            )

            LocalSessionClient.confirmSessionWithRetry(
                tabletUrl = tabletBaseUrl,
                sessionId = sessionId,
                userId = userId,
                userName = userName,
                userEmail = userEmail,
            ).also { result ->
                if (result.isSuccess) {
                    Log.d("LOCAL_CLIENT", "confirm_local_success traceId=$traceId sessionId=$sessionId cartId=${result.getOrNull()}")
                    Log.d(SmartCartLogTags.QR, "confirm_local_success traceId=$traceId sessionId=$sessionId cartId=${result.getOrNull()}")
                    QrFlowPhoneLog.d(
                        event = "qr_local_confirm_success",
                        "traceId" to traceId,
                        "sessionId" to sessionId,
                        "cartId" to result.getOrNull()
                    )
                } else {
                    Log.e("LOCAL_CLIENT", "confirm_local_failed traceId=$traceId sessionId=$sessionId error=${result.exceptionOrNull()?.message}")
                    Log.e(
                        SmartCartLogTags.QR,
                        "confirm_local_failed traceId=$traceId sessionId=$sessionId error=${result.exceptionOrNull()?.message}",
                        result.exceptionOrNull()
                    )
                    QrFlowPhoneLog.e(
                        event = "qr_local_confirm_failed",
                        throwable = result.exceptionOrNull(),
                        "traceId" to traceId,
                        "sessionId" to sessionId,
                        "tabletBaseUrl" to tabletBaseUrl
                    )
                }
            }
        }
    }

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
                Log.d(SmartCartLogTags.QR, "confirm_cloud_start traceId=$traceId sessionId=$sessionId")

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

                    val currentStatus = normalizeStatus(freshSession.getString("status")).ifBlank { "pending" }
                    val existingConfirmedUserId = freshSession.getString("confirmedUserId")?.trim().orEmpty()
                    val isIdempotentSameOwner = currentStatus == "confirmed" && existingConfirmedUserId == user.uid
                    val shouldWriteSessionConfirm = currentStatus == "pending"

                    when {
                        shouldWriteSessionConfirm -> Unit
                        isIdempotentSameOwner -> {
                            Log.d(
                                SmartCartLogTags.QR,
                                "confirm_idempotent_success traceId=$traceId sessionId=$sessionId userId=${user.uid}"
                            )
                        }
                        currentStatus == "confirmed" && existingConfirmedUserId.isNotBlank() && existingConfirmedUserId != user.uid -> {
                            Log.e(
                                SmartCartLogTags.QR,
                                "confirm_rejected_already_owned traceId=$traceId sessionId=$sessionId ownerUserId=$existingConfirmedUserId requesterUserId=${user.uid}"
                            )
                            throw IllegalStateException("Session already confirmed by another user")
                        }
                        else -> {
                            Log.e(
                                SmartCartLogTags.QR,
                                "invalid_session_transition actor=phone from=$currentStatus to=confirmed sessionId=$sessionId"
                            )
                            throw IllegalStateException(
                                "Invalid session transition for phone: $currentStatus -> confirmed"
                            )
                        }
                    }

                    if (shouldWriteSessionConfirm) {
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
                    }

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
                Log.d(SmartCartLogTags.QR, "confirm_cloud_success traceId=$traceId sessionId=$sessionId cartId=$cartId")

                Result.success(cartId)
            } catch (t: Throwable) {
                Log.e(SmartCartLogTags.QR, "confirm_cloud_failed traceId=$traceId sessionId=$sessionId", t)
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
