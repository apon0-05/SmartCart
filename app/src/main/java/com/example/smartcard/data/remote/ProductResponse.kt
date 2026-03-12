package com.example.smartcard.data.remote

data class ProductResponse(
    val name: String,
    val barcode: String,
    val brand: String,
    val price: Int
)