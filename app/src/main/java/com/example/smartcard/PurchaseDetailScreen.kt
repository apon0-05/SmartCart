package com.example.smartcard

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
fun PurchaseDetailScreen(
    receiptId: String,
    onBack: () -> Unit,
    onBottomHome: () -> Unit,
    onBottomBag: () -> Unit,
    onBottomCart: () -> Unit,
    onBottomHistory: () -> Unit
) {
    val texts = LocalAppStrings.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var receiptData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val bg = Color(0xFFF6F6F6)
    val textDark = Color(0xFF2F2F2F)
    val green = Color(0xFF39D10A)

    LaunchedEffect(receiptId) {
        val user = auth.currentUser
        if (user == null) {
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
                    error = texts.purchaseNotFound
                }
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
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Text(
                    text = "${texts.errorLabel}: $error",
                    color = Color.Red
                )
                Spacer(Modifier.weight(1f))
            }

            receiptData != null -> {
                val purchaseTime = receiptData?.get("purchaseTime") as? String ?: ""
                val totalAmount = (receiptData?.get("totalAmount") as? Number)?.toDouble() ?: 0.0

                @Suppress("UNCHECKED_CAST")
                val items = receiptData?.get("items") as? List<Map<String, Any>> ?: emptyList()

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

                Text(
                    text = texts.purchaseInformation,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textDark,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(22.dp))

                Text(
                    text = texts.locationLabel,
                    color = textDark,
                    fontSize = 14.sp
                )
                Text(
                    text = texts.locationValue,
                    color = textDark,
                    fontSize = 18.sp
                )

                Spacer(Modifier.height(14.dp))

                Text(
                    text = texts.purchaserLabel,
                    color = textDark,
                    fontSize = 14.sp
                )
                Text(
                    text = FirebaseAuth.getInstance().currentUser?.email ?: texts.unknown,
                    color = textDark,
                    fontSize = 18.sp
                )

                Spacer(Modifier.height(14.dp))

                Text(
                    text = texts.dateTimeLabel,
                    color = textDark,
                    fontSize = 14.sp
                )
                Text(
                    text = purchaseTime,
                    color = textDark,
                    fontSize = 18.sp
                )

                Spacer(Modifier.height(20.dp))

                Text(
                    text = texts.yourPurchase,
                    color = textDark,
                    fontSize = 16.sp
                )

                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🟢", fontSize = 14.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = texts.paid,
                        color = green,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(Modifier.height(14.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items.forEach { item ->
                        PurchaseDetailRow(
                            item = item,
                            textDark = textDark,
                            currency = texts.tenge
                        )
                    }
                }

                HorizontalDivider()
                Spacer(Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = texts.amount,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textDark
                    )
                    Text(
                        text = "${totalAmount.toInt()} ${texts.tenge}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textDark
                    )
                }

                Spacer(Modifier.height(18.dp))
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
private fun PurchaseDetailRow(
    item: Map<String, Any>,
    textDark: Color,
    currency: String
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
                .background(Color(0xFFF4F4F4)),
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
            Text(
                text = name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = textDark
            )
            Text(
                text = "${price.toInt()} $currency",
                fontSize = 14.sp,
                color = textDark
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${rowTotal.toInt()} $currency",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = textDark
            )
            Text(
                text = "x $quantity",
                fontSize = 14.sp,
                color = textDark
            )
        }
    }
}