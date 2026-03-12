package com.example.smartcard.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface ProductApi {
    @GET("products/by-barcode/{barcode}")
    suspend fun getProductByBarcode(@Path("barcode") barcode: String): ProductResponse
}