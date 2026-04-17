package com.example.smartcard

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartcard.localization.LocalAppStrings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

private fun resolveAvatar(gender: String?): Int {
    return if (gender?.trim()?.equals("male", ignoreCase = true) == true) {
        R.drawable.avatar_male
    } else {
        R.drawable.avatar_female
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    userFullName: String,
    onProfileClick: () -> Unit,
    onScanCartClick: () -> Unit,
    onScanProductClick: () -> Unit,
    onReceiptClick: () -> Unit,
    onProductsPurchasedClick: () -> Unit,
    onNavigateToCleanState: () -> Unit,
) {
    val texts = LocalAppStrings.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()

    val connectedCartId by CartConnectionSession.connectedCartIdFlow.collectAsState()
    val tabletSessionId by CartConnectionSession.tabletSessionIdFlow.collectAsState()
    val latestNavigateToCleanState by rememberUpdatedState(onNavigateToCleanState)

    LaunchedEffect(currentUser?.uid, connectedCartId, tabletSessionId) {
        val user = currentUser ?: return@LaunchedEffect
        if (!connectedCartId.isNullOrBlank() || !tabletSessionId.isNullOrBlank()) {
            return@LaunchedEffect
        }

        Log.d(
            SmartCartLogTags.SESSION_FLOW,
            "restart_recovery_start uid=${user.uid}"
        )

        db.collection("carts")
            .whereEqualTo("connectedUserId", user.uid)
            .whereIn("status", listOf("connected", "active"))
            .get()
            .addOnSuccessListener { cartsSnap ->
                val cartDoc = cartsSnap.documents.firstOrNull()
                if (cartDoc == null) {
                    Log.d(
                        SmartCartLogTags.SESSION_FLOW,
                        "restart_recovery_rejected reason=no_connected_cart uid=${user.uid}"
                    )
                    return@addOnSuccessListener
                }

                val cartId = cartDoc.id.trim()
                val cartStatus = cartDoc.getString("status")?.trim()?.lowercase().orEmpty()
                val sessionId = cartDoc.getString("sessionId")?.trim().orEmpty()

                if (cartId.isBlank()) {
                    Log.d(
                        SmartCartLogTags.SESSION_FLOW,
                        "restart_recovery_rejected reason=blank_cart_id uid=${user.uid}"
                    )
                    return@addOnSuccessListener
                }

                if (cartStatus !in setOf("connected", "active")) {
                    Log.d(
                        SmartCartLogTags.SESSION_FLOW,
                        "restart_recovery_rejected reason=cart_status_invalid status=$cartStatus cartId=$cartId uid=${user.uid}"
                    )
                    return@addOnSuccessListener
                }

                if (sessionId.isBlank()) {
                    Log.d(
                        SmartCartLogTags.SESSION_FLOW,
                        "restart_recovery_rejected reason=missing_session_id cartId=$cartId uid=${user.uid}"
                    )
                    return@addOnSuccessListener
                }

                db.collection("tabletSessions")
                    .document(sessionId)
                    .get()
                    .addOnSuccessListener { sessionSnap ->
                        if (!sessionSnap.exists()) {
                            Log.d(
                                SmartCartLogTags.SESSION_FLOW,
                                "restart_recovery_rejected reason=session_not_found sessionId=$sessionId cartId=$cartId uid=${user.uid}"
                            )
                            return@addOnSuccessListener
                        }

                        val sessionStatus = sessionSnap.getString("status")?.trim()?.lowercase().orEmpty()
                        val sessionCartId = sessionSnap.getString("cartId")?.trim().orEmpty()

                        if (sessionStatus !in setOf("confirmed", "active", "checkout_in_progress")) {
                            Log.d(
                                SmartCartLogTags.SESSION_FLOW,
                                "restart_recovery_rejected reason=session_status_invalid status=$sessionStatus sessionId=$sessionId cartId=$cartId uid=${user.uid}"
                            )
                            return@addOnSuccessListener
                        }

                        if (sessionCartId.isBlank() || sessionCartId != cartId) {
                            Log.d(
                                SmartCartLogTags.SESSION_FLOW,
                                "restart_recovery_rejected reason=session_cart_mismatch sessionCartId=$sessionCartId cartId=$cartId sessionId=$sessionId uid=${user.uid}"
                            )
                            return@addOnSuccessListener
                        }

                        CartConnectionSession.updateConnection(
                            cartId = cartId,
                            sessionId = sessionId
                        )
                        Log.d(
                            SmartCartLogTags.SESSION_FLOW,
                            "restart_recovery_success cartId=$cartId sessionId=$sessionId sessionStatus=$sessionStatus uid=${user.uid}"
                        )
                    }
                    .addOnFailureListener { e ->
                        Log.e(
                            SmartCartLogTags.SESSION_FLOW,
                            "restart_recovery_failed reason=session_fetch_error sessionId=$sessionId cartId=$cartId uid=${user.uid}",
                            e
                        )
                    }
            }
            .addOnFailureListener { e ->
                Log.e(
                    SmartCartLogTags.SESSION_FLOW,
                    "restart_recovery_failed reason=cart_query_error uid=${user.uid}",
                    e
                )
            }
    }

    fun clearSession(reason: String, navigateToCleanState: Boolean = false) {
        Log.d(
            SmartCartLogTags.SESSION_FLOW,
            "cleared reason=$reason cartId=${connectedCartId.orEmpty()} sessionId=${tabletSessionId.orEmpty()}"
        )
        CartConnectionSession.updateConnection(null)
        if (navigateToCleanState) {
            latestNavigateToCleanState()
        }
    }

    DisposableEffect(tabletSessionId, connectedCartId) {
        val sid = tabletSessionId
        val cid = connectedCartId
        val hasSession = !sid.isNullOrBlank()
        val hasCart = !cid.isNullOrBlank()

        if (hasSession.xor(hasCart)) {
            clearSession(reason = "invalid_pair_state")
            return@DisposableEffect onDispose { }
        }

        if (!hasSession || !hasCart) {
            return@DisposableEffect onDispose { }
        }

        val registration = db.collection("tabletSessions")
            .document(sid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(SmartCartLogTags.SESSION_FLOW, "listener error", error)
                    return@addSnapshotListener
                }

                if (snapshot == null || !snapshot.exists()) {
                    clearSession("session_not_found")
                    return@addSnapshotListener
                }

                val snapshotCartId = snapshot.getString("cartId")?.trim().orEmpty()
                if (snapshotCartId.isBlank() || snapshotCartId != cid) {
                    clearSession("session_cart_invalid_or_mismatch")
                    return@addSnapshotListener
                }

                val status = snapshot.getString("status")?.lowercase()
                if (status.isNullOrBlank()) {
                    clearSession("unsupported_terminal_status_missing")
                    return@addSnapshotListener
                }

                when (status) {
                    "pending", "confirmed", "active" -> Unit
                    "completed" -> {
                        clearSession("session_completed", navigateToCleanState = true)
                        return@addSnapshotListener
                    }
                    else -> {
                        clearSession("unsupported_terminal_status_$status")
                        return@addSnapshotListener
                    }
                }
            }

        onDispose {
            registration.remove()
        }
    }

    val isConnected = !connectedCartId.isNullOrBlank()

    var purchaseCount by remember { mutableStateOf(0) }
    var isLoadingPurchases by remember { mutableStateOf(true) }
    var avatarGender by remember { mutableStateOf<String?>(null) }

    DisposableEffect(currentUser?.uid) {
        val user = currentUser ?: return@DisposableEffect onDispose { }

        val registration = db.collection("users")
            .document(user.uid)
            .addSnapshotListener { document, _ ->
                val storedGender = document?.getString("avatarGender")?.trim()?.lowercase()
                val normalizedGender = if (storedGender == "male" || storedGender == "female") {
                    storedGender
                } else {
                    "female"
                }

                avatarGender = normalizedGender

                if (storedGender == null || (storedGender != "male" && storedGender != "female")) {
                    db.collection("users")
                        .document(user.uid)
                        .set(mapOf("avatarGender" to "female"), SetOptions.merge())
                }
            }

        onDispose {
            registration.remove()
        }
    }

    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            FirebaseFirestore.getInstance().collection("users")
                .document(user.uid)
                .collection("purchases")
                .get()
                .addOnSuccessListener { result ->
                    purchaseCount = result.size()
                    isLoadingPurchases = false
                }
                .addOnFailureListener {
                    purchaseCount = 0
                    isLoadingPurchases = false
                }
        } else {
            purchaseCount = 0
            isLoadingPurchases = false
        }
    }

    val purchasesText = when {
        isLoadingPurchases -> texts.loading
        purchaseCount == 0 -> texts.noPurchasesYet
        else -> "$purchaseCount ${texts.purchases}"
    }

    val heroTitleFontSize = if (texts.welcomeTo.length > 16) 26.sp else 32.sp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        // ── Top content area ──────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(24.dp))

            // ── Header: greeting + avatar ─────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = texts.goodAfternoon,
                        color = AppColors.TextDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = true
                    )
                    Text(
                        text = userFullName,
                        color = AppColors.TextDark,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = true
                    )
                }
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF0F1F5))
                        .clickable { onProfileClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = resolveAvatar(avatarGender)),
                        contentDescription = null,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── WELCOME TO title ──────────────────────────────────────────────
            Text(
                text = texts.welcomeTo,
                color = AppColors.TextDark,
                fontSize = heroTitleFontSize,
                lineHeight = (heroTitleFontSize.value + 4).sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                softWrap = true
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = texts.smart,
                    color = AppColors.TextDark,
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false
                )
                Text(
                    text = texts.shopping,
                    color = AppColors.Primary,
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false
                )
                Text(
                    text = texts.cart,
                    color = AppColors.TextDark,
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false
                )
            }

            Spacer(Modifier.height(20.dp))

            HomePromoBanner()

            Spacer(Modifier.height(20.dp))

            // ── Feature cards row ─────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HomeFeatureCard(
                    modifier = Modifier.weight(1f),
                    iconBg = AppColors.SoftBlue,
                    icon = Icons.Outlined.QrCodeScanner,
                    iconTint = Color(0xFF6C63FF),
                    title = texts.scanProduct,
                    subtitle = texts.continueShopping,
                    onClick = if (isConnected) onScanProductClick else onScanCartClick
                )
                HomeFeatureCard(
                    modifier = Modifier.weight(1f),
                    iconBg = AppColors.SoftOrange,
                    icon = Icons.Outlined.ReceiptLong,
                    iconTint = AppColors.Primary,
                    title = texts.receiptTaxInvoice,
                    subtitle = texts.receipts,
                    onClick = onReceiptClick
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Products purchased big card ───────────────────────────────────
            HomeBigCard(
                iconBg = AppColors.SoftMint,
                icon = Icons.Outlined.ShoppingBag,
                iconTint = AppColors.Success,
                title = texts.productsPurchased,
                subtitle = purchasesText,
                onClick = onProductsPurchasedClick
            )

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun HomePromoBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(122.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.CardWarm)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == 1) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == 1) AppColors.Primary.copy(alpha = 0.75f)
                            else AppColors.PrimaryLight.copy(alpha = 0.85f)
                        )
                )
            }
        }
    }
}

@Composable
private fun HomeFeatureCard(
    modifier: Modifier = Modifier,
    iconBg: Color,
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .height(176.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.Surface)
            .clickable { onClick() }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = title,
            color = AppColors.TextDark,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            softWrap = true
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = subtitle,
            color = AppColors.TextHint,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            softWrap = true
        )
    }
}

@Composable
private fun HomeBigCard(
    iconBg: Color,
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.Surface)
            .clickable { onClick() }
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = title,
            color = AppColors.TextDark,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            softWrap = true
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = subtitle,
            color = AppColors.TextHint,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            softWrap = true
        )
    }
}
