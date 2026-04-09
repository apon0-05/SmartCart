package com.example.smartcard

import android.net.Uri

object QrPayloadParser {

    data class Parsed(
        val traceId: String,
        val parseStrategy: String,
        val sessionId: String? = null,
        val cartId: String? = null
    )

    fun parse(raw: String, fallbackTraceId: String): Parsed {
        val trimmed = raw.trim()

        if (trimmed.startsWith("sess_", ignoreCase = true)) {
            return Parsed(
                traceId = fallbackTraceId,
                parseStrategy = "raw_session_id",
                sessionId = trimmed
            )
        }

        // Strategy 1: session://smartcart/<sessionId>?traceId=...
        runCatching {
            val uri = Uri.parse(trimmed)
            if (uri.scheme.equals("session", ignoreCase = true) &&
                uri.host.equals("smartcart", ignoreCase = true)
            ) {
                val sessionId = uri.pathSegments.firstOrNull()?.takeIf { it.isNotBlank() }
                val traceId = uri.getQueryParameter("traceId")?.takeIf { it.isNotBlank() } ?: fallbackTraceId
                return Parsed(
                    traceId = traceId,
                    parseStrategy = "session_uri",
                    sessionId = sessionId
                )
            }
        }

        // Strategy 2: URL with ?sessionId=...&traceId=...
        runCatching {
            val uri = Uri.parse(trimmed)
            val sessionId = uri.getQueryParameter("sessionId")?.takeIf { it.isNotBlank() }
            val cartId = uri.getQueryParameter("cartId")?.takeIf { it.isNotBlank() }
            val traceId = uri.getQueryParameter("traceId")?.takeIf { it.isNotBlank() } ?: fallbackTraceId
            if (sessionId != null || cartId != null) {
                return Parsed(
                    traceId = traceId,
                    parseStrategy = "query_params",
                    sessionId = sessionId,
                    cartId = cartId
                )
            }
        }

        // Strategy 3: plain cartId (legacy behavior)
        return Parsed(
            traceId = fallbackTraceId,
            parseStrategy = "raw_cart_id",
            cartId = trimmed.takeIf { it.isNotBlank() }
        )
    }
}
