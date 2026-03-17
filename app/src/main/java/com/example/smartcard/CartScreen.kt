package com.example.smartcard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

@Composable
fun CartScreen(
    onBack: () -> Unit,
    onGoPayment: (String) -> Unit,
    onBottomHome: () -> Unit,
    onBottomBag: () -> Unit,
    onBottomCart: () -> Unit,
    onBottomHistory: () -> Unit
) {
    val bg = Color(0xFFF6F6F6)
    val cardBg = Color(0xFFF4F4F4)
    val textDark = Color(0xFF2F2F2F)
    val accent = Color(0xFFCF6B2D)

    var isPaying by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val cartId = CartConnectionSession.connectedCartId

    DisposableEffect(cartId) {
        if (cartId.isNullOrBlank()) {
            CartSession.replaceAll(emptyList())
            onDispose { }
        } else {
            val registration = RemoteCartRepository.listenToCart(
                cartId = cartId,
                onCartChanged = { remoteItems ->
                    CartSession.replaceAll(remoteItems)
                },
                onError = { message ->
                    error = message
                }
            )

            onDispose {
                registration.remove()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        // Top bar
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

            Spacer(Modifier.width(12.dp))

            Text(
                text = "Shopping Cart 🛒",
                color = textDark,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp
            )
        }

        Spacer(Modifier.height(18.dp))

        // Items
        CartSession.items.forEach { item ->
            CartRow(
                item = item,
                onInc = {
                    if (!cartId.isNullOrBlank()) {
                        RemoteCartRepository.increaseQty(cartId, CartSession.items.toList(), item.barcode)
                    }
                },
                onDec = {
                    if (!cartId.isNullOrBlank()) {
                        RemoteCartRepository.decreaseQty(cartId, CartSession.items.toList(), item.barcode)
                    }
                },
                onRemove = {
                    if (!cartId.isNullOrBlank()) {
                        RemoteCartRepository.removeItem(cartId, CartSession.items.toList(), item.barcode)
                    }
                },
                cardBg = cardBg,
                accent = accent
            )
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(10.dp))

        // Total
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Total", color = textDark, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(
                "${CartSession.total()} TG",
                color = accent,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
            )
        }

        Spacer(Modifier.height(16.dp))
        if (error != null) {
            Text(
                text = error!!,
                color = Color.Red,
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }
        // Payment button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFFF1C2A6), accent)
                    )
                )
                .clickable(enabled = !isPaying && CartSession.items.isNotEmpty()) {
                    isPaying = true
                    error = null

                    PurchaseRepository.savePurchase(
                        cartItems = CartSession.items,
                        onSuccess = { receiptId ->
                            ReceiptSession.lastReceiptId = receiptId

                            if (!cartId.isNullOrBlank()) {
                                RemoteCartRepository.clearCart(cartId,
                                    onSuccess = {
                                        CartConnectionRepository.disconnectCart(
                                            cartId = cartId,
                                            onSuccess = {
                                                CartSession.clear()
                                                onGoPayment(receiptId)
                                                isPaying = false
                                            },
                                            onError = {
                                                CartSession.clear()
                                                onGoPayment(receiptId)
                                                isPaying = false
                                            }
                                        )
                                    },
                                    onError = {
                                        CartSession.clear()
                                        onGoPayment(receiptId)
                                        isPaying = false
                                    }
                                )
                            } else {
                                CartSession.clear()
                                onGoPayment(receiptId)
                                isPaying = false
                            }
                        },
                        onError = { message ->
                            error = "Payment failed: $message"
                            isPaying = false
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Go To Payment",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(Modifier.weight(1f))

        // Bottom nav (как в HomeScreen)
        BottomNavBar(
            onHome = onBottomHome,
            onBag = onBottomBag,
            onCart = onBottomCart,
            onHistory = onBottomHistory
        )
    }
}

@Composable
private fun CartRow(
    item: CartItem,
    onInc: () -> Unit,
    onDec: () -> Unit,
    onRemove: () -> Unit,
    cardBg: Color,
    accent: Color
) {
    val textDark = Color(0xFF2F2F2F)
    val hint = Color(0xFF8A8A8A)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // image placeholder
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(cardBg),
            contentAlignment = Alignment.Center
        ) {
            if (item.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = item.imageEmoji,
                    fontSize = 22.sp
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(item.brand ?: " ", color = hint, fontSize = 12.sp)
            Text(item.name, color = textDark, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(4.dp))
            Text("${item.price} TG", color = accent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        // qty control
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(cardBg)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("−", modifier = Modifier.clickable { onDec() }, fontSize = 18.sp)
            Spacer(Modifier.width(10.dp))
            Text("${item.qty}", fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(10.dp))
            Text("+", modifier = Modifier.clickable { onInc() }, fontSize = 18.sp)
        }

        Spacer(Modifier.width(10.dp))

        Text("🗑️", modifier = Modifier.clickable { onRemove() }, fontSize = 18.sp)
    }
}

@Composable
fun ReceiptScreen(
    receiptId: String,
    onBack: () -> Unit
) {
    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

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
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    receiptData = document.data
                } else {
                    error = "Receipt not found"
                }
                isLoading = false
            }
            .addOnFailureListener { e ->
                error = e.message ?: "Failed to load receipt"
                isLoading = false
            }
    }

    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        error != null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Column {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Error: $error", color = Color.Red)
                }
            }
        }

        receiptData != null -> {
            val receiptIdValue = receiptData?.get("receiptId") as? String ?: ""
            val purchaseTime = receiptData?.get("purchaseTime") as? String ?: ""
            val totalAmount = (receiptData?.get("totalAmount") as? Number)?.toDouble() ?: 0.0
            val totalItems = (receiptData?.get("totalItems") as? Number)?.toInt() ?: 0

            @Suppress("UNCHECKED_CAST")
            val items = receiptData?.get("items") as? List<Map<String, Any>> ?: emptyList()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF7F7F7))
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("‹", fontSize = 22.sp)
                    }
                }

                Spacer(Modifier.height(20.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .padding(24.dp)
                ) {
                    Text(
                        "MAGNUM SHOPPING CENTER",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text("Receipt ID: $receiptIdValue", fontSize = 14.sp)
                    Text("Time: $purchaseTime", fontSize = 14.sp)

                    Spacer(Modifier.height(24.dp))

                    Text(
                        "RECEIPT / TAX INVOICE",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp
                    )

                    Spacer(Modifier.height(24.dp))

                    items.forEach { item ->
                        val name = item["name"] as? String ?: ""
                        val quantity  = (item["quantity"] as? Number)?.toInt() ?: 0
                        val price = (item["price"] as? Number)?.toDouble() ?: 0.0
                        val rowTotal = quantity  * price

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("$quantity    $name", fontSize = 16.sp)
                            Text("${rowTotal.toInt()} TG", fontSize = 16.sp)
                        }
                        Spacer(Modifier.height(10.dp))
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total  $totalItems items", fontWeight = FontWeight.Medium)
                        Text("${totalAmount.toInt()} TG", fontWeight = FontWeight.Medium)
                    }

                    Spacer(Modifier.height(40.dp))

                    Text(
                        "MAGNUM SHOPPING CENTER",
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        purchaseTime,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

