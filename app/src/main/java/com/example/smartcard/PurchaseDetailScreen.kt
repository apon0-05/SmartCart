package com.example.smartcard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

@Composable
fun PurchaseDetailScreen(
    receiptId: String,
    onBack: () -> Unit,
    onBottomHome: () -> Unit,
    onBottomBag: () -> Unit,
    onBottomCart: () -> Unit,
    onBottomHistory: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var receiptData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(receiptId) {
        val user = auth.currentUser
        if (user == null) {
            error = "User not logged in"
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
                    error = "Purchase not found"
                }
                isLoading = false
            }
            .addOnFailureListener { e ->
                error = e.message ?: "Failed to load purchase"
                isLoading = false
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        // Back button always visible
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppBackButton(onClick = onBack)
            Spacer(Modifier.width(12.dp))
            Text(
                "Purchase Details",
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

            receiptData != null -> {
                val purchaseTime = receiptData?.get("purchaseTime") as? String ?: ""
                val totalAmount = (receiptData?.get("totalAmount") as? Number)?.toDouble() ?: 0.0

                @Suppress("UNCHECKED_CAST")
                val items = receiptData?.get("items") as? List<Map<String, Any>> ?: emptyList()

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(AppRadius.Large)
                            .background(AppColors.Surface)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DetailInfoRow(label = "Purchaser", value = FirebaseAuth.getInstance().currentUser?.email ?: "Unknown")
                        DetailInfoRow(label = "Date", value = purchaseTime)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Status", color = AppColors.TextHint, fontSize = 13.sp)
                            Spacer(Modifier.weight(1f))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AppColors.Success,
                                modifier = Modifier.size(16.dp)
                            )
                            Text("Paid", color = AppColors.Success, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        "Items",
                        color = AppColors.TextDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(AppRadius.Large)
                            .background(AppColors.Surface)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items.forEachIndexed { idx, item ->
                            PurchaseDetailRow(item = item)
                            if (idx < items.lastIndex) {
                                HorizontalDivider(color = AppColors.SurfaceAlt)
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(AppRadius.Large)
                            .background(AppColors.Surface)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextDark)
                        Text("${totalAmount.toInt()} ₸", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = AppColors.Primary)
                    }

                    Spacer(Modifier.height(12.dp))
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
private fun DetailInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = AppColors.TextHint, fontSize = 13.sp)
        Text(value, color = AppColors.TextDark, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun PurchaseDetailRow(item: Map<String, Any>) {
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
                .size(48.dp)
                .clip(AppRadius.Medium)
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
                Text(text = emoji.ifBlank { "🛍️" }, fontSize = 22.sp)
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextDark)
            Text("${price.toInt()} ₸", fontSize = 13.sp, color = AppColors.TextHint)
        }

        Column(horizontalAlignment = Alignment.End) {
            Text("${rowTotal.toInt()} ₸", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppColors.TextDark)
            Text("×$quantity", fontSize = 12.sp, color = AppColors.TextHint)
        }
    }
}