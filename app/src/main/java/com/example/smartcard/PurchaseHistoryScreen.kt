package com.example.smartcard

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.smartcard.localization.LocalAppStrings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun PurchaseHistoryScreen(
    onBack: () -> Unit,
    onOpenPurchase: (String) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var purchases by remember { mutableStateOf<List<PurchaseHistoryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val texts = LocalAppStrings.current

    LaunchedEffect(Unit) {
        val user = auth.currentUser
        if (user == null) {
            error = texts.userNotLoggedIn
            isLoading = false
            return@LaunchedEffect
        }

        db.collection("users")
            .document(user.uid)
            .collection("purchases")
            .get()
            .addOnSuccessListener { result ->
                purchases = result.documents.map { doc ->
                    @Suppress("UNCHECKED_CAST")
                    PurchaseHistoryItem(
                        receiptId = doc.getString("receiptId") ?: doc.id,
                        purchaseTime = doc.getString("purchaseTime") ?: "",
                        totalAmount = (doc.get("totalAmount") as? Number)?.toDouble() ?: 0.0,
                        totalItems = (doc.get("totalItems") as? Number)?.toInt() ?: 0,
                        items = doc.get("items") as? List<Map<String, Any>> ?: emptyList()
                    )
                }.sortedByDescending { it.purchaseTime }

                isLoading = false
            }
            .addOnFailureListener { e ->
                Log.e(SmartCartLogTags.PAYMENT, "history_load_failed uid=${user.uid}", e)
                QrFlowPhoneLog.e(
                    event = "history_load_failed",
                    throwable = e,
                    "where" to "users/{uid}/purchases"
                )
                error = friendlyHistoryError(e, texts)
                isLoading = false
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppBackButton(onClick = onBack)
            Spacer(Modifier.weight(1f))
            Text(
                text = texts.orderCompleted,
                color = AppColors.TextSubtle,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1
            )
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = texts.history,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = AppColors.TextDark
        )

        Spacer(Modifier.height(16.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.Primary)
                }
            }

            error != null -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = texts.couldNotLoadHistory,
                        color = AppColors.Error,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = error!!,
                        color = AppColors.TextSubtle,
                        fontSize = 13.sp
                    )
                }
                Spacer(Modifier.weight(1f))
            }

            purchases.isEmpty() -> {
                Text(texts.noPurchasesYet, color = AppColors.TextSubtle)
                Spacer(Modifier.weight(1f))
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(purchases) { purchase ->
                        PurchaseHistoryCard(
                            purchase = purchase,
                            onClick = { onOpenPurchase(purchase.receiptId) }
                        )
                    }
                }
            }
        }
    }
}

private fun friendlyHistoryError(error: Throwable, texts: com.example.smartcard.localization.AppStrings): String {
    val msg = error.message?.lowercase().orEmpty()
    return if ("permission" in msg || "denied" in msg) {
        texts.historyAccessTemporarilyUnavailable
    } else {
        texts.historyCheckConnectionAndRetry
    }
}

@Composable
private fun PurchaseHistoryCard(
    purchase: PurchaseHistoryItem,
    onClick: () -> Unit
) {
    val texts = LocalAppStrings.current
    val firstItem = purchase.items.firstOrNull()
    val imageUrl = firstItem?.get("imageUrl") as? String ?: ""
    val emoji = firstItem?.get("imageEmoji") as? String ?: "🛍️"
    val itemName = firstItem?.get("name") as? String ?: firstItem?.get("productName") as? String ?: "—"
    val rightTop = "${purchase.totalItems.coerceAtLeast(1)} ${texts.itemsLabel}: ${purchase.totalAmount.toInt()} ${texts.currency}"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.SurfaceAlt)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AppColors.Surface),
            contentAlignment = Alignment.Center
        ) {
            if (imageUrl.isNotBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = itemName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(text = if (emoji.isNotBlank()) emoji else "🛍️", fontSize = 28.sp)
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = itemName,
                color = AppColors.TextDark,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = "${texts.orderNumberPrefix}${purchase.receiptId.take(8)}",
                color = AppColors.TextSubtle,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = rightTop,
                color = AppColors.TextDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = purchase.purchaseTime,
                color = AppColors.TextHint,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}
