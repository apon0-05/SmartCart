package com.example.smartcard.ml

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream

fun bitmapToFile(context: Context, bitmap: Bitmap): File {
    val file = File(context.cacheDir, "ml_frame_${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        out.flush()
    }
    return file
}