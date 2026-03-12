package com.example.smartcard.data.remote

data class CheckoutItemRequest(
    val barcode: String,
    val qty: Int
)

data class CheckoutRequest(
    val user_id: Int,
    val location: String?,
    val items: List<CheckoutItemRequest>
)

data class CheckoutResponse(
    val purchase_id: Int,
    val created_at: String,
    val location: String?,
    val total: Int
)

data class ReceiptItem(
    val name: String,
    val brand: String,
    val barcode: String,
    val price: Int,
    val qty: Int,
    val sum: Int
)

data class ReceiptResponse(
    val purchase_id: Int,
    val user_id: Int,
    val created_at: String,
    val location: String?,
    val items: List<ReceiptItem>,
    val total: Int
)