package com.example.smartcard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

@Composable
fun HomeScreen(
    userFullName: String,
    onProfileClick: () -> Unit,
    onScanProductClick: () -> Unit,
    onReceiptClick: () -> Unit,
    onProductsPurchasedClick: () -> Unit,
    onBottomHome: () -> Unit,
    onBottomBag: () -> Unit,
    onBottomCart: () -> Unit,
    onBottomHistory: () -> Unit
) {
    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    val db   = com.google.firebase.firestore.FirebaseFirestore.getInstance()

    var purchaseCount by remember { mutableStateOf(0) }
    var isLoadingPurchases by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val user = auth.currentUser
        if (user != null) {
            db.collection("users")
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

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else      -> "Good evening"
    }

    val purchasesText = if (purchaseCount == 1) "1 purchase" else "$purchaseCount purchases"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                )
            )
            .padding(vertical = 14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Surface)
                .padding(horizontal = 18.dp, vertical = 18.dp)
        ) {
            // ── Header ──────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = greeting,
                        color = AppColors.TextHint,
                        fontSize = 14.sp
                    )
                    Text(
                        text = userFullName,
                        color = AppColors.TextDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(AppColors.CardWarm)
                        .clickable { onProfileClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = AppColors.Primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Title ────────────────────────────────────────────────────────────
            Text(
                text = "SmartCart",
                color = AppColors.TextDark,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Scan. Shop. Done.",
                color = AppColors.Primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(18.dp))

            // ── Promo Banner ─────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp)
                    .clip(AppRadius.Large)
                    .background(AppColors.CardWarm)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Column {
                    Text(
                        text = "Shop faster",
                        color = AppColors.TextDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Scan products directly\nfrom your phone",
                        color = AppColors.TextHint,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(AppColors.Primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = AppColors.Primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // ── Feature Cards Row ────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                FeatureCard(
                    modifier = Modifier.weight(1f),
                    iconBg = Color(0xFFE8F0FF),
                    icon = Icons.Default.QrCodeScanner,
                    iconTint = Color(0xFF3A6FD8),
                    title = "Scan product",
                    subtitle = "Use barcode scanner",
                    onClick = onScanProductClick
                )
                FeatureCard(
                    modifier = Modifier.weight(1f),
                    iconBg = Color(0xFFFFE9DF),
                    icon = Icons.Default.Receipt,
                    iconTint = AppColors.Primary,
                    title = "My receipts",
                    subtitle = "View history",
                    onClick = onReceiptClick
                )
            }

            Spacer(Modifier.height(14.dp))

            // ── Big Card ─────────────────────────────────────────────────────────
            BigFeatureCard(
                iconBg = AppColors.SuccessBg,
                icon = Icons.Default.CheckCircle,
                iconTint = AppColors.Success,
                title = "Products purchased",
                subtitle = if (isLoadingPurchases) "Loading..." else purchasesText,
                onClick = onProductsPurchasedClick
            )

            Spacer(Modifier.weight(1f))

            BottomNavBar(
                currentTab = NavTab.HOME,
                onHome = onBottomHome,
                onBag = onBottomBag,
                onCart = onBottomCart,
                onHistory = onBottomHistory
            )
        }
    }
}

@Composable
private fun FeatureCard(
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
            .height(140.dp)
            .clip(AppRadius.Large)
            .background(AppColors.CardWarm)
            .clickable { onClick() }
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(AppRadius.Medium)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = title,
            color = AppColors.TextDark,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = subtitle,
            color = AppColors.TextHint,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun BigFeatureCard(
    iconBg: Color,
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppRadius.Large)
            .background(AppColors.CardWarm)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(AppRadius.Medium)
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
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                color = AppColors.TextDark,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = AppColors.TextHint,
                fontSize = 13.sp
            )
        }
    }
}
