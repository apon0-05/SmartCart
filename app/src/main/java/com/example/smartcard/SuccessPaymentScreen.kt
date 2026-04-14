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
import com.example.smartcard.localization.LocalAppStrings

@Composable
fun SuccessPaymentScreen(
    receiptId: String,
    onDownloadReceipt: (String) -> Unit,
    onBackHome: () -> Unit
) {
    val texts = LocalAppStrings.current

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

        // TITLE
        Text(
            text = texts.successful,
            color = textDark,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 26.sp
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = texts.paymentSuccessDesc,
            color = hint,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(26.dp))

        // ICON
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

        // CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                Text(
                    text = texts.purchaseId,
                    color = hint,
                    fontSize = 12.sp
                )

                Text(
                    text = if (receiptId.isNotBlank()) receiptId else texts.notAvailable,
                    color = textDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        // DOWNLOAD BUTTON
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
                text = texts.downloadReceipt,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(Modifier.height(12.dp))

        // BACK BUTTON
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
            Text(texts.backToHome, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))

        // WARNING
        if (receiptId.isBlank()) {
            Text(
                text = texts.receiptUnavailable,
                color = Color.Red,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}