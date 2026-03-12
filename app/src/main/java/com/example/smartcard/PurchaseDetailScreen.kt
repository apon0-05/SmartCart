package com.example.smartcard

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
            .background(bg)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        when {
            isLoading -> {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Text("Error: $error", color = Color.Red)
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
                    "Purchase information",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textDark,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(22.dp))

                Text("Location:", color = textDark, fontSize = 14.sp)
                Text("Almaty, Kazakhstan", color = textDark, fontSize = 18.sp)

                Spacer(Modifier.height(14.dp))

                Text("Purchaser:", color = textDark, fontSize = 14.sp)
                Text(FirebaseAuth.getInstance().currentUser?.email ?: "Unknown", color = textDark, fontSize = 18.sp)

                Spacer(Modifier.height(14.dp))

                Text("Date and time:", color = textDark, fontSize = 14.sp)
                Text(purchaseTime, color = textDark, fontSize = 18.sp)

                Spacer(Modifier.height(20.dp))

                Text("Your purchase:", color = textDark, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🟢", fontSize = 14.sp)
                    Spacer(Modifier.width(6.dp))
                    Text("Paid", color = green, fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                    Text("Amount", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = textDark)
                    Text("${totalAmount.toInt()} ₸", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = textDark)
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
    textDark: Color
) {
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
            Text(emoji, fontSize = 24.sp)
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textDark)
            Text("${price.toInt()} ₸", fontSize = 14.sp, color = textDark)
        }

        Column(horizontalAlignment = Alignment.End) {
            Text("${rowTotal.toInt()} ₸", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = textDark)
            Text("x $quantity", fontSize = 14.sp, color = textDark)
        }
    }
}