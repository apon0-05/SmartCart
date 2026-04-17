package com.example.smartcard

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object CartConnectionSession {
    private val _connectedCartId = MutableStateFlow<String?>(null)
    val connectedCartIdFlow: StateFlow<String?> = _connectedCartId.asStateFlow()

    private val _tabletSessionId = MutableStateFlow<String?>(null)
    val tabletSessionIdFlow: StateFlow<String?> = _tabletSessionId.asStateFlow()

    var connectedCartId: String?
        get() = _connectedCartId.value
        private set(value) {
            _connectedCartId.value = value
        }

    var tabletSessionId: String?
        get() = _tabletSessionId.value
        private set(value) {
            _tabletSessionId.value = value
        }

    var lastKnownConnectedCartId: String? = null

    fun updateConnection(cartId: String?, sessionId: String? = tabletSessionId) {
        connectedCartId = cartId
        tabletSessionId = if (cartId.isNullOrBlank()) null else sessionId
        if (!cartId.isNullOrBlank()) {
            lastKnownConnectedCartId = cartId
        }
    }
}