package com.example.smartcard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

@Composable
fun PurchaseHistoryScreen(
    onBack: () -> Unit,
    onOpenPurchase: (String) -> Unit,
    onBottomHome: () -> Unit,
    onBottomBag: () -> Unit,
    onBottomCart: () -> Unit,
    onBottomHistory: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var purchases by remember { mutableStateOf<List<PurchaseHistoryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val user = auth.currentUser
        if (user == null) {
            error = "User not logged in"
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
                error = e.message ?: "Failed to load history"
                isLoading = false
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppBackButton(onClick = onBack)
            Spacer(Modifier.width(12.dp))
            Text(
                "History",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AppColors.TextDark
            )
        }

        Spacer(Modifier.height(18.dp))

        when {
            isLoading -> AppLoadingState(modifier = Modifier.weight(1f))

            error != null -> {
                AppErrorState(message = error!!)
                Spacer(Modifier.weight(1f))
            }

            purchases.isEmpty() -> {
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(AppRadius.Large)
                            .background(AppColors.CardWarm),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = AppColors.Primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No purchases yet",
                        color = AppColors.TextDark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Your purchase history will appear here",
                        color = AppColors.TextHint,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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

        BottomNavBar(
            currentTab = NavTab.HISTORY,
            onHome = onBottomHome,
            onBag = onBottomBag,
            onCart = onBottomCart,
            onHistory = onBottomHistory
        )
    }
}

@Composable
private fun PurchaseHistoryCard(
    purchase: PurchaseHistoryItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppRadius.Large)
            .background(AppColors.Surface)
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .clip(AppRadius.Small)
                    .background(AppColors.SuccessBg)
                    .padding(horizontal = 12.dp, vertical = 3.dp)
            ) {
                Text(
                    "Paid",
                    color = AppColors.Success,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                purchase.purchaseTime,
                color = AppColors.TextDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                purchase.items.take(4).forEach { item ->
                    val imageUrl = item["imageUrl"] as? String ?: ""
                    val emoji = item["imageEmoji"] as? String ?: "🛍️"
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(AppRadius.Small)
                            .background(AppColors.SurfaceAlt),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(text = emoji.ifBlank { "🛍️" }, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        Text(
            text = "${purchase.totalAmount.toInt()} ₸",
            color = AppColors.Primary,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}