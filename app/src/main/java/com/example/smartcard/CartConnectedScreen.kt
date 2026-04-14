package com.example.smartcard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CartConnectedScreen(
    cartId: String,
    onBackHome: () -> Unit
) {
    val resolvedCartId = cartId.takeIf { it.isNotBlank() }
        ?: CartConnectionSession.connectedCartId
        ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(AppColors.SuccessBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                tint = AppColors.Success,
                modifier = Modifier.size(52.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Cart Connected!",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = AppColors.TextDark
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your phone is connected to the cart.",
            fontSize = 14.sp,
            color = AppColors.TextHint,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(AppRadius.Large)
                .background(AppColors.Surface)
                .padding(16.dp)
        ) {
            Text("Cart ID", color = AppColors.TextHint, fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (resolvedCartId.isNotBlank()) resolvedCartId else "Not available",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextDark
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        PrimaryButton(
            text = "Back to Home",
            onClick = onBackHome
        )
    }
}