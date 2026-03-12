package com.example.smartcard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SuccessPaymentScreen(
    receiptId: String,
    onDownloadReceipt: (String) -> Unit,
    onBackHome: () -> Unit
){
    val bg = Color(0xFFF6F6F6)
    val textDark = Color(0xFF2F2F2F)
    val hint = Color(0xFF7A7A7A)
    val accent = Color(0xFFCF6B2D)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(18.dp))

        // верхний заголовок
        Text(
            text = "Successful ✅",
            color = textDark,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 26.sp
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = "Your payment was completed successfully",
            color = hint,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(26.dp))

        // большая иконка
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(Color(0xFFEAF7F0)),
            contentAlignment = Alignment.Center
        ) {
            Text("✅", fontSize = 58.sp)
        }

        Spacer(Modifier.height(26.dp))

        // карточка с purchase id (можно убрать, но удобно для дебага)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Purchase ID",
                    color = hint,
                    fontSize = 12.sp
                )
                Text(
                    text = if (receiptId.isNotBlank()) receiptId else "Not available",
                    color = textDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        // Download receipt
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFFF1C2A6), accent))
                )
                .clickable(enabled = receiptId.isNotBlank()) {
                    onDownloadReceipt(receiptId)
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Download receipt",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(Modifier.height(12.dp))

        // Back to home
        OutlinedButton(
            onClick = onBackHome,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = accent
            )
        ) {
            Text("Back to home", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))

        // если purchaseId == 0, показываем подсказку
        if (receiptId.isBlank()) {
            Text(
                text = "⚠️ Receipt is unavailable because purchaseId = 0.\nMake sure you save purchase_id after checkout.",
                color = Color.Red,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}