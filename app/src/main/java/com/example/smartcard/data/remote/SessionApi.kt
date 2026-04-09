package com.example.smartcard.data.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface SessionApi {

    @Deprecated(
        message = "Legacy endpoint. Do not use for QR login. Use Firestore tabletSessions flow via QrSessionRepository.confirmSession instead.",
        level = DeprecationLevel.ERROR
    )
    @POST("api/session/confirm")
    suspend fun confirmSession(
        @Header("X-Trace-Id") traceId: String,
        @Body body: ConfirmSessionRequest
    ): Response<ResponseBody>
}

data class ConfirmSessionRequest(
    val sessionId: String,
    val userId: String? = null,
    val email: String? = null
)
