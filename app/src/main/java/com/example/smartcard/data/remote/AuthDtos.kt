package com.example.smartcard.data.remote

data class RegisterRequest(
    val full_name: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val id: Int,
    val full_name: String,
    val email: String
)

data class Product(
    val name: String = "",
    val brand: String = "",
    val price: Double = 0.0,
    val barcode: String = ""
)

data class PurchaseHistoryItem(
    val receiptId: String = "",
    val purchaseTime: String = "",
    val totalAmount: Double = 0.0,
    val totalItems: Int = 0,
    val items: List<Map<String, Any>> = emptyList()
)

