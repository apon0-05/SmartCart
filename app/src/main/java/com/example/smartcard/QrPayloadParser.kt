package com.example.smartcard

import android.net.Uri

object QrPayloadParser {

    private const val LOCAL_DEFAULT_PORT = 8080

    data class Parsed(
        val traceId: String,
        val parseStrategy: String,
        val sessionId: String? = null,
        val cartId: String? = null,
        val localUrl: String? = null,    // full "http://<ip>:8080" base URL when local mode
    )

    fun parse(raw: String, fallbackTraceId: String): Parsed {
        val trimmed = raw.trim()

        // Strategy 0: local server URL — http://<ip>:<port>/session/<sessionId>
        // Example: http://192.168.1.42:8080/session/sess_abc123
        runCatching {
            if (trimmed.startsWith("http://", ignoreCase = true) ||
                trimmed.startsWith("https://", ignoreCase = true)
            ) {
                val uri = Uri.parse(trimmed)
                val segments = uri.pathSegments
                // expect ["session", "<sessionId>"]
                if (segments.size >= 2 && segments[0].equals("session", ignoreCase = true)) {
                    val sessionId = segments[1].takeIf { it.isNotBlank() }
                    if (sessionId != null) {
                        val host = uri.host?.trim().orEmpty()
                        val scheme = uri.scheme?.trim().orEmpty()
                        if (host.isNotBlank() && scheme.isNotBlank()) {
                            val port = if (uri.port > 0) uri.port else LOCAL_DEFAULT_PORT
                            val baseUrl = "$scheme://$host:$port"
                            return Parsed(
                                traceId = fallbackTraceId,
                                parseStrategy = "local_server_url",
                                sessionId = sessionId,
                                localUrl = baseUrl,
                            )
                        }
                    }
                }

                // Strategy 0b: local URL with query sessionId (defensive compatibility)
                val qpSessionId = uri.getQueryParameter("sessionId")?.takeIf { it.isNotBlank() }
                if (qpSessionId != null) {
                    val host = uri.host?.trim().orEmpty()
                    val scheme = uri.scheme?.trim().orEmpty()
                    if (host.isNotBlank() && scheme.isNotBlank()) {
                        val port = if (uri.port > 0) uri.port else LOCAL_DEFAULT_PORT
                        val baseUrl = "$scheme://$host:$port"
                        return Parsed(
                            traceId = fallbackTraceId,
                            parseStrategy = "local_server_query_url",
                            sessionId = qpSessionId,
                            localUrl = baseUrl,
                        )
                    }
                }
            }
        }

        if (trimmed.startsWith("sess_", ignoreCase = true) ||
            trimmed.startsWith("local_", ignoreCase = true)
        ) {
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
