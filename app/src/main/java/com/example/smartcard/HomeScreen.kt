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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartcard.localization.LocalAppStrings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.smartcard.utils.LanguageManager

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
    val texts = LocalAppStrings.current

    val bg = Color(0xFFF6F6F6)
    val card = Color(0xFFF3E9E6)
    val textDark = Color(0xFF2F2F2F)
    val accent = Color(0xFFCF6B2D)
    val softBlue = Color(0xFFE8F0FF)
    val softOrange = Color(0xFFFFE9DF)
    val softMint = Color(0xFFE6F7F1)

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

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

    val purchasesText = if (isLoadingPurchases) {
        texts.loading
    } else {
        when (LanguageManager.getLanguage()) {
            "ru" -> when {
                purchaseCount % 10 == 1 && purchaseCount % 100 != 11 -> "$purchaseCount покупка"
                purchaseCount % 10 in 2..4 && purchaseCount % 100 !in 12..14 -> "$purchaseCount покупки"
                else -> "$purchaseCount покупок"
            }

            "kk" -> "$purchaseCount сатып алу"
            else -> if (purchaseCount == 1) "$purchaseCount purchase" else "$purchaseCount purchases"
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
                .background(Color.White)
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = texts.goodAfternoon,
                        color = textDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = userFullName,
                        color = Color(0xFF7A7A7A),
                        fontSize = 14.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDDE3FF))
                        .clickable { onProfileClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("👤", fontSize = 18.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = texts.welcomeTo,
                color = textDark,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = texts.smart + " ",
                    color = textDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = texts.shopping,
                    color = accent,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = " " + texts.cart,
                    color = textDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(card)
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(4) { idx ->
                    Box(
                        modifier = Modifier
                            .size(if (idx == 1) 7.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (idx == 1) accent else Color(0xFFE4B9A8)
                            )
                    )
                    Spacer(Modifier.width(6.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                FeatureCard(
                    modifier = Modifier.weight(1f),
                    background = card,
                    iconBg = softBlue,
                    icon = "🧾",
                    title = texts.scanProduct,
                    subtitle = texts.scannerCount,
                    onClick = onScanProductClick
                )

                FeatureCard(
                    modifier = Modifier.weight(1f),
                    background = card,
                    iconBg = softOrange,
                    icon = "🧾",
                    title = texts.receiptTaxInvoice,
                    subtitle = texts.detectedCount,
                    onClick = onReceiptClick
                )
            }

            Spacer(Modifier.height(14.dp))

            BigFeatureCard(
                background = card,
                iconBg = softMint,
                icon = "✅",
                title = texts.productsPurchased,
                subtitle = purchasesText,
                onClick = onProductsPurchasedClick
            )

            Spacer(Modifier.weight(1f))

            BottomNavBar(
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
    background: Color,
    iconBg: Color,
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val textDark = Color(0xFF2F2F2F)
    val hint = Color(0xFF7A7A7A)

    Column(
        modifier = modifier
            .height(140.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(background)
            .clickable { onClick() }
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 18.sp)
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = title,
            color = textDark,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = subtitle,
            color = hint,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun BigFeatureCard(
    background: Color,
    iconBg: Color,
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val textDark = Color(0xFF2F2F2F)
    val hint = Color(0xFF7A7A7A)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(background)
            .clickable { onClick() }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 20.sp)
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = title,
            color = textDark,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = subtitle,
            color = hint,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun BottomNavBar(
    onHome: () -> Unit,
    onBag: () -> Unit,
    onCart: () -> Unit,
    onHistory: () -> Unit
) {
    val barBg = Color(0xFFF4F4F4)
    val active = Color(0xFF2DA1FF)
    val inactive = Color(0xFF9A9A9A)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(barBg)
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomIcon(activeBg = true, icon = "🏠", tint = active, onClick = onHome)
        BottomIcon(activeBg = false, icon = "📷", tint = inactive, onClick = onBag)
        BottomIcon(activeBg = false, icon = "🛒", tint = inactive, onClick = onCart)
        BottomIcon(activeBg = false, icon = "🕘", tint = inactive, onClick = onHistory)
    }
}

@Composable
private fun BottomIcon(
    activeBg: Boolean,
    icon: String,
    tint: Color,
    onClick: () -> Unit
) {
    val bg = if (activeBg) Color(0xFFE1F0FF) else Color.Transparent

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            fontSize = 18.sp,
            color = tint
        )
    }
}