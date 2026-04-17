package com.example.smartcard

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartcard.localization.LocalAppStrings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
@Composable
fun PurchaseDetailScreen(
    receiptId: String,
    onBack: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val texts = LocalAppStrings.current

    var receiptData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val bg = AppColors.Background
    val textDark = AppColors.TextDark
    val green = AppColors.Success

    LaunchedEffect(receiptId) {
        val user = auth.currentUser
        if (user == null) {
            Log.e(SmartCartLogTags.PAYMENT, "purchase_detail_load_failed reason=user_not_logged_in receiptId=$receiptId")
            error = texts.userNotLoggedIn
            isLoading = false
            return@LaunchedEffect
        }

        db.collection("users")
            .document(user.uid)
            .collection("purchases")
            .document(receiptId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    receiptData = doc.data
                } else {
                    Log.e(SmartCartLogTags.PAYMENT, "purchase_detail_load_failed reason=not_found receiptId=$receiptId uid=${user.uid}")
                    error = texts.purchaseNotFound
                }
                isLoading = false
            }
            .addOnFailureListener { e ->
                Log.e(SmartCartLogTags.PAYMENT, "purchase_detail_load_failed reason=firestore_error receiptId=$receiptId uid=${user.uid}", e)
                error = e.message ?: texts.failedToLoadPurchase
                isLoading = false
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(18.dp))
                .background(AppColors.Surface)
                .padding(18.dp)
        ) {
            when {
                isLoading -> {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                error != null -> {
                    Text("${texts.errorLabel}: $error", color = AppColors.Error)
                    Spacer(Modifier.weight(1f))
                }

                receiptData != null -> {
                    val purchaseTime = receiptData?.get("purchaseTime") as? String ?: ""
                    val totalAmount = (receiptData?.get("totalAmount") as? Number)?.toDouble() ?: 0.0

                    @Suppress("UNCHECKED_CAST")
                    val items = receiptData?.get("items") as? List<Map<String, Any>> ?: emptyList()

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppBackButton(onClick = onBack)
                    }

                    Spacer(Modifier.height(18.dp))

                    Text(
                        texts.purchaseInformation,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textDark,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(Modifier.height(22.dp))

                    Text(texts.locationLabel, color = textDark, fontSize = 14.sp)
                    Text(texts.locationValue, color = textDark, fontSize = 18.sp)

                    Spacer(Modifier.height(14.dp))

                    Text(texts.purchaserLabel, color = textDark, fontSize = 14.sp)
                    Text(FirebaseAuth.getInstance().currentUser?.email ?: texts.unknown, color = textDark, fontSize = 18.sp)

                    Spacer(Modifier.height(14.dp))

                    Text(texts.dateTimeLabel, color = textDark, fontSize = 14.sp)
                    Text(purchaseTime, color = textDark, fontSize = 18.sp)

                    Spacer(Modifier.height(20.dp))

                    Text(texts.yourPurchase, color = textDark, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🟢", fontSize = 14.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(texts.paid, color = green, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }

                    Spacer(Modifier.height(14.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items.forEach { item ->
                            PurchaseDetailRow(item = item, textDark = textDark)
                        }
                    }

                    HorizontalDivider()
                    Spacer(Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(texts.amount, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = textDark)
                        Text("${totalAmount.toInt()} ${texts.tenge}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = textDark)
                    }

                    Spacer(Modifier.height(18.dp))
                }
            }

            Spacer(Modifier.height(2.dp))
        }
    }
}

@Composable
private fun PurchaseDetailRow(
    item: Map<String, Any>,
    textDark: Color
) {
    val imageUrl = item["imageUrl"] as? String ?: ""
    val emoji = item["imageEmoji"] as? String ?: "🛍️"
    val name = item["name"] as? String ?: ""
    val quantity = (item["quantity"] as? Number)?.toInt() ?: 0
    val price = (item["price"] as? Number)?.toDouble() ?: 0.0
    val rowTotal = quantity * price

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AppColors.SurfaceAlt),
            contentAlignment = Alignment.Center
        ) {
            if (imageUrl.isNotBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = if (emoji.isNotBlank()) emoji else "🛍️",
                    fontSize = 24.sp
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textDark)
            Text("${price.toInt()} ${LocalAppStrings.current.tenge}", fontSize = 14.sp, color = textDark)
        }

        Column(horizontalAlignment = Alignment.End) {
            Text("${rowTotal.toInt()} ${LocalAppStrings.current.tenge}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = textDark)
            Text("x $quantity", fontSize = 14.sp, color = textDark)
        }
    }
}