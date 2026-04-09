package com.example.smartcard.data.remote

import com.example.smartcard.QrFlowPhoneLog
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.UUID
import kotlin.jvm.java

object ApiClient {

    // ✅ ВСТАВЬ тот адрес, который ОТКРЫЛСЯ в браузере эмулятора:
    // Если работает 10.0.2.2:
    // private const val BASE_URL = "http://10.0.2.2:8001/"
    //
    // Если работает твой IP:
    private const val BASE_URL = "http://10.5.5.20:8001/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.NONE
    }

    private class QrFlowInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val encodedPath = request.url.encodedPath

            val isQrEndpoint = encodedPath.startsWith("/api/session")
            if (!isQrEndpoint) {
                return chain.proceed(request)
            }

            val existingTraceId = request.header("X-Trace-Id")
            val traceId = existingTraceId ?: ("trace_" + UUID.randomUUID().toString().replace("-", "").take(8))

            val requestBodyString = runCatching {
                val body = request.body ?: return@runCatching null
                val buffer = Buffer()
                body.writeTo(buffer)
                buffer.readUtf8()
            }.getOrNull()

            QrFlowPhoneLog.d(
                event = "backend_request_start",
                "endpoint" to request.url.toString(),
                "method" to request.method,
                "requestBody" to requestBodyString,
                "traceId" to traceId
            )

            val requestWithTrace = request.newBuilder()
                .header("X-Trace-Id", traceId)
                .build()

            return try {
                val response = chain.proceed(requestWithTrace)

                val peeked = runCatching { response.peekBody(1024 * 1024).string() }.getOrNull()

                QrFlowPhoneLog.d(
                    event = "backend_response",
                    "httpCode" to response.code,
                    "responseBody" to peeked,
                    "traceId" to traceId
                )

                response
            } catch (t: Throwable) {
                QrFlowPhoneLog.e(
                    event = "exception",
                    throwable = t,
                    "where" to "QrFlowInterceptor",
                    "endpoint" to request.url.toString(),
                    "method" to request.method,
                    "traceId" to traceId
                )
                throw t
            }
        }
    }

    private val http = OkHttpClient.Builder()
        .addInterceptor(QrFlowInterceptor())
        .addInterceptor(logging)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(http)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApi: AuthApi = retrofit.create(AuthApi::class.java)
    val productApi: ProductApi = retrofit.create(ProductApi::class.java)
    val purchaseApi: PurchaseApi = retrofit.create(PurchaseApi::class.java)
    val sessionApi: SessionApi = retrofit.create(SessionApi::class.java)
}
