package com.example.smartcard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.smartcard.data.remote.PurchaseHistoryItem
import com.example.smartcard.localization.LocalAppStrings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun PurchaseHistoryScreen(
    onBack: () -> Unit,
    onOpenPurchase: (String) -> Unit,
    onBottomHome: () -> Unit,
    onBottomBag: () -> Unit,
    onBottomCart: () -> Unit,
    onBottomHistory: () -> Unit
) {
    val texts = LocalAppStrings.current

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var purchases by remember { mutableStateOf<List<PurchaseHistoryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val bg = Color(0xFFF6F6F6)
    val textDark = Color(0xFF2F2F2F)
    val hint = Color(0xFF7A7A7A)
    val green = Color(0xFF39D10A)

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
                error = e.message ?: texts.failedToLoadPurchase
                isLoading = false
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {

        // BACK
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Text("‹", fontSize = 22.sp, color = textDark)
            }
        }

        Spacer(Modifier.height(18.dp))

        // TITLE
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = texts.history,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textDark
            )

            Text(
                text = texts.orderCompleted,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = textDark
            )
        }

        Spacer(Modifier.height(18.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Text(error!!, color = Color.Red)
                Spacer(Modifier.weight(1f))
            }

            purchases.isEmpty() -> {
                Text(texts.noPurchases, color = hint)
                Spacer(Modifier.weight(1f))
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(purchases) { purchase ->
                        PurchaseHistoryCard(
                            purchase = purchase,
                            green = green,
                            textDark = textDark,
                            hint = hint,
                            texts = texts,
                            onClick = { onOpenPurchase(purchase.receiptId) }
                        )
                    }
                }
            }
        }

        BottomNavBar(
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
    green: Color,
    textDark: Color,
    hint: Color,
    texts: com.example.smartcard.localization.AppStrings,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Column {

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(green)
                    .padding(horizontal = 20.dp, vertical = 2.dp)
            ) {
                Text(
                    text = texts.paid,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(purchase.purchaseTime, color = textDark, fontSize = 14.sp)

            Text(texts.locationValue, color = hint, fontSize = 12.sp)

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                purchase.items.take(4).forEach { item ->

                    val imageUrl = item["imageUrl"] as? String ?: ""
                    val emoji = item["imageEmoji"] as? String ?: "🛍️"

                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFF4F4F4)),
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
                            Text(
                                text = if (emoji.isNotBlank()) emoji else "🛍️",
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        Text(
            text = "${purchase.totalAmount.toInt()} ${texts.tenge}",
            color = textDark,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}