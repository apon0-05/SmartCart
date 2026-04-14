package com.example.smartcard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
                onCartChanged = { remoteItems -> CartSession.replaceAll(remoteItems) },
                onError = { message -> error = message }
            )
            onDispose { registration.remove() }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        // ── Top bar ──────────────────────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppBackButton(onClick = onBack)
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Shopping Cart",
                color = AppColors.TextDark,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp
            )
        }

        Spacer(Modifier.height(18.dp))

        // ── Items or empty state ─────────────────────────────────────────────
        if (CartSession.items.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(AppRadius.Large)
                        .background(AppColors.CardWarm),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = AppColors.Primary,
                        modifier = Modifier.size(38.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Your cart is empty",
                    color = AppColors.TextDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Scan products to add them here",
                    color = AppColors.TextHint,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                CartSession.items.forEach { item ->
                    CartRow(
                        item = item,
                        onInc = {
                            if (!cartId.isNullOrBlank())
                                RemoteCartRepository.increaseQty(cartId, CartSession.items.toList(), item.barcode)
                        },
                        onDec = {
                            if (!cartId.isNullOrBlank())
                                RemoteCartRepository.decreaseQty(cartId, CartSession.items.toList(), item.barcode)
                        },
                        onRemove = {
                            if (!cartId.isNullOrBlank())
                                RemoteCartRepository.removeItem(cartId, CartSession.items.toList(), item.barcode)
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                }

                Spacer(Modifier.height(8.dp))

                // ── Total ────────────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(AppRadius.Medium)
                        .background(AppColors.Surface)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Total",
                        color = AppColors.TextDark,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Text(
                        "${CartSession.total()} ₸",
                        color = AppColors.Primary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                }

                Spacer(Modifier.height(12.dp))

                if (error != null) {
                    AppErrorState(message = error!!)
                    Spacer(Modifier.height(10.dp))
                }

                PrimaryButton(
                    text = if (isPaying) "Processing…" else "Go to Payment",
                    enabled = !isPaying && CartSession.items.isNotEmpty(),
                    onClick = {
                        isPaying = true
                        error = null
                        PurchaseRepository.savePurchase(
                            cartItems = CartSession.items,
                            onSuccess = { receiptId ->
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
                    }
                )

                Spacer(Modifier.height(12.dp))
            }
        }

        BottomNavBar(
            currentTab = NavTab.CART,
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
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppRadius.Large)
            .background(AppColors.Surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(AppRadius.Medium)
                .background(AppColors.SurfaceAlt),
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
                Text(text = item.imageEmoji, fontSize = 22.sp)
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            if (!item.brand.isNullOrBlank()) {
                Text(item.brand, color = AppColors.TextHint, fontSize = 11.sp)
            }
            Text(
                item.name,
                color = AppColors.TextDark,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${item.price} ₸",
                color = AppColors.Primary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        // Qty control
        Row(
            modifier = Modifier
                .clip(AppRadius.Medium)
                .background(AppColors.SurfaceAlt)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "−",
                modifier = Modifier.clickable { onDec() },
                fontSize = 18.sp,
                color = AppColors.TextDark,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "${item.qty}",
                fontWeight = FontWeight.Bold,
                color = AppColors.TextDark,
                fontSize = 15.sp
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "+",
                modifier = Modifier.clickable { onInc() },
                fontSize = 18.sp,
                color = AppColors.TextDark,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(AppRadius.Medium)
                .background(Color(0xFFFFF0EE))
                .clickable { onRemove() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Remove",
                tint = AppColors.Error,
                modifier = Modifier.size(18.dp)
            )
        }
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
                "Receipt",
                color = AppColors.TextDark,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp
            )
        }

        Spacer(Modifier.height(18.dp))

        when {
            isLoading -> AppLoadingState(modifier = Modifier.weight(1f))

            error != null -> {
                AppErrorState(message = error!!, onRetry = null)
                Spacer(Modifier.weight(1f))
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
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(AppRadius.Large)
                        .background(AppColors.Surface)
                        .padding(20.dp)
                ) {
                    Text(
                        "SmartCart",
                        color = AppColors.Primary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("Receipt / Tax Invoice", color = AppColors.TextHint, fontSize = 13.sp)

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = AppColors.SurfaceAlt)
                    Spacer(Modifier.height(12.dp))

                    if (receiptIdValue.isNotBlank()) {
                        ReceiptMetaRow(label = "Receipt ID", value = receiptIdValue)
                        Spacer(Modifier.height(6.dp))
                    }
                    if (purchaseTime.isNotBlank()) {
                        ReceiptMetaRow(label = "Date", value = purchaseTime)
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = AppColors.SurfaceAlt)
                    Spacer(Modifier.height(12.dp))

                    items.forEach { item ->
                        val name     = item["name"] as? String ?: ""
                        val quantity = (item["quantity"] as? Number)?.toInt() ?: 0
                        val price    = (item["price"] as? Number)?.toDouble() ?: 0.0
                        val rowTotal = quantity * price

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(name, color = AppColors.TextDark, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text("× $quantity", color = AppColors.TextHint, fontSize = 12.sp)
                            }
                            Text(
                                "${rowTotal.toInt()} ₸",
                                color = AppColors.TextDark,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = AppColors.SurfaceAlt)
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "$totalItems items",
                            color = AppColors.TextHint,
                            fontSize = 14.sp
                        )
                        Text(
                            "${totalAmount.toInt()} ₸",
                            color = AppColors.Primary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptMetaRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = AppColors.TextHint, fontSize = 13.sp)
        Text(value, color = AppColors.TextDark, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

