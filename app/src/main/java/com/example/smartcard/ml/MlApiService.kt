package com.example.smartcard.ml

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface MlApiService {

    @Multipart
    @POST("predict")
    suspend fun predictProduct(
        @Part image: MultipartBody.Part,
        @Part("barcode_product") barcodeProduct: RequestBody
    ): Response<MlPredictResponse>
}