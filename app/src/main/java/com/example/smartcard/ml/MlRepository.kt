package com.example.smartcard.ml

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

object MlRepository {

    suspend fun sendFrameToMl(
        file: File,
        barcodeProduct: String = "unknown"
    ): MlPredictResponse? {
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())

        val imagePart = MultipartBody.Part.createFormData(
            "image",
            file.name,
            requestFile
        )

        val barcodeBody =
            barcodeProduct.toRequestBody("text/plain".toMediaTypeOrNull())

        val response = MlApiClient.api.predictProduct(
            image = imagePart,
            barcodeProduct = barcodeBody
        )

        return if (response.isSuccessful) response.body() else null
    }
}