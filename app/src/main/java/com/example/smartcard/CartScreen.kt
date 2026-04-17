package com.example.smartcard

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.smartcard.localization.LocalAppStrings

@Composable
fun CartScreen(
    onBack: () -> Unit,
    onGoPayment: (String) -> Unit
) {
    val texts = LocalAppStrings.current
    val bg = AppColors.Background
    val cardBg = AppColors.SurfaceAlt
    val textDark = AppColors.TextDark
    val accent = AppColors.Primary

    var isPaying by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val cartId = CartConnectionSession.connectedCartId

    fun finishCheckoutWithCleanupOutcome(
        receiptId: String,
        remoteCartResetSuccess: Boolean,
        disconnectSuccess: Boolean,
        disconnectError: String? = null,
        cartResetError: String? = null,
    ) {
        val outcome = when {
            remoteCartResetSuccess && disconnectSuccess -> "success"
            remoteCartResetSuccess || disconnectSuccess -> "partial_success"
            else -> "failure"
        }

        Log.d(
            SmartCartLogTags.PAYMENT,
            "checkout_cleanup_result outcome=$outcome cartId=${cartId.orEmpty()} receiptId=$receiptId cartResetSuccess=$remoteCartResetSuccess disconnectSuccess=$disconnectSuccess"
        )
        if (!cartResetError.isNullOrBlank()) {
            Log.e(
                SmartCartLogTags.PAYMENT,
                "checkout_cleanup_cart_reset_failed cartId=${cartId.orEmpty()} receiptId=$receiptId reason=$cartResetError"
            )
        }
        if (!disconnectError.isNullOrBlank()) {
            Log.e(
                SmartCartLogTags.PAYMENT,
                "checkout_cleanup_disconnect_failed cartId=${cartId.orEmpty()} receiptId=$receiptId reason=$disconnectError"
            )
        }

        CartSession.clear()
        onGoPayment(receiptId)
        isPaying = false
    }

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
            // Top bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppBackButton(onClick = onBack)

                Spacer(Modifier.width(12.dp))

                Text(
                    text = texts.shoppingCartTitle,
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
                Text(texts.total, color = textDark, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    "${CartSession.total()} ${texts.currency}",
                    color = accent,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            }

            Spacer(Modifier.height(16.dp))
            if (error != null) {
                Text(
                    text = error!!,
                    color = AppColors.Error,
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
                            listOf(AppColors.PrimaryLight, accent)
                        )
                    )
                    .clickable(enabled = !isPaying && CartSession.items.isNotEmpty()) {
                        isPaying = true
                        error = null

                        PurchaseRepository.savePurchase(
                            cartItems = CartSession.items,
                            onSuccess = { receiptId ->
                                ReceiptSession.lastReceiptId = receiptId
                                Log.d(
                                    SmartCartLogTags.PAYMENT,
                                    "checkout_purchase_persisted receiptId=$receiptId cartId=${cartId.orEmpty()}"
                                )

                                if (!cartId.isNullOrBlank()) {
                                    RemoteCartRepository.clearCart(cartId,
                                        onSuccess = {
                                            Log.d(
                                                SmartCartLogTags.PAYMENT,
                                                "checkout_cleanup_cart_reset_success cartId=$cartId receiptId=$receiptId"
                                            )
                                            CartConnectionRepository.disconnectCart(
                                                cartId = cartId,
                                                onSuccess = {
                                                    Log.d(
                                                        SmartCartLogTags.PAYMENT,
                                                        "checkout_cleanup_disconnect_success cartId=$cartId receiptId=$receiptId"
                                                    )
                                                    finishCheckoutWithCleanupOutcome(
                                                        receiptId = receiptId,
                                                        remoteCartResetSuccess = true,
                                                        disconnectSuccess = true
                                                    )
                                                },
                                                onError = { disconnectErr ->
                                                    finishCheckoutWithCleanupOutcome(
                                                        receiptId = receiptId,
                                                        remoteCartResetSuccess = true,
                                                        disconnectSuccess = false,
                                                        disconnectError = disconnectErr
                                                    )
                                                }
                                            )
                                        },
                                        onError = { cartResetErr ->
                                            Log.e(
                                                SmartCartLogTags.PAYMENT,
                                                "checkout_cleanup_cart_reset_failed cartId=$cartId receiptId=$receiptId reason=$cartResetErr"
                                            )
                                            CartConnectionRepository.disconnectCart(
                                                cartId = cartId,
                                                onSuccess = {
                                                    Log.d(
                                                        SmartCartLogTags.PAYMENT,
                                                        "checkout_cleanup_disconnect_success_after_cart_reset_failure cartId=$cartId receiptId=$receiptId"
                                                    )
                                                    finishCheckoutWithCleanupOutcome(
                                                        receiptId = receiptId,
                                                        remoteCartResetSuccess = false,
                                                        disconnectSuccess = true,
                                                        cartResetError = cartResetErr
                                                    )
                                                },
                                                onError = { disconnectErr ->
                                                    finishCheckoutWithCleanupOutcome(
                                                        receiptId = receiptId,
                                                        remoteCartResetSuccess = false,
                                                        disconnectSuccess = false,
                                                        disconnectError = disconnectErr,
                                                        cartResetError = cartResetErr
                                                    )
                                                }
                                            )
                                        }
                                    )
                                } else {
                                    Log.d(
                                        SmartCartLogTags.PAYMENT,
                                        "checkout_cleanup_skipped reason=no_cart_connection receiptId=$receiptId"
                                    )
                                    finishCheckoutWithCleanupOutcome(
                                        receiptId = receiptId,
                                        remoteCartResetSuccess = true,
                                        disconnectSuccess = true
                                    )
                                }
                            },
                            onError = { message ->
                                error = "${texts.paymentFailed}: $message"
                                isPaying = false
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    texts.goToPayment,
                    color = AppColors.Surface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.weight(1f))
        }
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
    val texts = LocalAppStrings.current
    val textDark = AppColors.TextDark
    val hint = AppColors.TextHint

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(AppColors.Surface)
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
            Text("${item.price} ${texts.currency}", color = accent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
    val texts = com.example.smartcard.localization.LocalAppStrings.current

    var receiptData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(receiptId) {
        val user = auth.currentUser

        if (user == null) {
            Log.e(SmartCartLogTags.PAYMENT, "receipt_load_failed reason=user_not_logged_in receiptId=$receiptId")
            error = texts.userNotLoggedIn
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
                    Log.e(SmartCartLogTags.PAYMENT, "receipt_load_failed reason=not_found receiptId=$receiptId uid=${user.uid}")
                    error = texts.receiptNotFound
                }
                isLoading = false
            }
            .addOnFailureListener { e ->
                Log.e(SmartCartLogTags.PAYMENT, "receipt_load_failed reason=firestore_error receiptId=$receiptId uid=${user.uid}", e)
                error = e.message ?: texts.failedToLoadReceipt
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
                        Text(texts.back)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("${texts.errorLabel}: $error", color = AppColors.Error)
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
                    .background(AppColors.Background)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppBackButton(onClick = onBack)
                }

                Spacer(Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 340.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppColors.Surface)
                            .padding(horizontal = 14.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = texts.shopName,
                            color = AppColors.TextDark,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            lineHeight = 16.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 2
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${texts.receiptTaxInvoice}: —",
                            color = AppColors.TextHint,
                            fontSize = 11.sp,
                            lineHeight = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${texts.receiptIdLabel}: ${receiptIdValue.take(12)}",
                            color = AppColors.TextHint,
                            fontSize = 11.sp,
                            lineHeight = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )

                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(color = AppColors.GoogleBorder.copy(alpha = 0.7f), thickness = 0.8.dp)
                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = texts.receiptTaxInvoice.uppercase(),
                            color = AppColors.TextDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            lineHeight = 15.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )

                        Spacer(Modifier.height(8.dp))

                        items.forEach { item ->
                            val name = item["name"] as? String ?: ""
                            val quantity = (item["quantity"] as? Number)?.toInt() ?: 0
                            val price = (item["price"] as? Number)?.toDouble() ?: 0.0
                            val rowTotal = quantity * price
                            val displayName = if (quantity > 1) "$name ×$quantity" else name

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = displayName,
                                    color = AppColors.TextDark,
                                    fontSize = 12.sp,
                                    lineHeight = 14.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "${rowTotal.toInt()} ${texts.currency}",
                                    color = AppColors.TextDark,
                                    fontSize = 12.sp,
                                    lineHeight = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1
                                )
                            }
                            Spacer(Modifier.height(5.dp))
                        }

                        HorizontalDivider(color = AppColors.GoogleBorder.copy(alpha = 0.7f), thickness = 0.8.dp)
                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${texts.total} $totalItems ${texts.itemsLabel}",
                                color = AppColors.TextDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                lineHeight = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "${totalAmount.toInt()} ${texts.currency}",
                                color = AppColors.TextDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                lineHeight = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1
                            )
                        }

                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(color = AppColors.GoogleBorder.copy(alpha = 0.7f), thickness = 0.8.dp)
                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "${texts.timeLabel}: $purchaseTime",
                            color = AppColors.TextHint,
                            fontSize = 11.sp,
                            lineHeight = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }
}













