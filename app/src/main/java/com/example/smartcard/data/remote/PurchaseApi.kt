package com.example.smartcard.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PurchaseApi {
    @POST("purchases/checkout")
    suspend fun checkout(@Body req: CheckoutRequest): CheckoutResponse

    @GET("purchases/{purchaseId}/receipt")
    suspend fun getReceipt(@Path("purchaseId") purchaseId: Int): ReceiptResponse
}