package com.example.smartcard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(AppColors.SuccessBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = AppColors.Success,
                modifier = androidx.compose.ui.Modifier.size(60.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Payment Successful",
            color = AppColors.TextDark,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Your purchase has been completed",
            color = AppColors.TextHint,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(28.dp))

        Column(
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .clip(AppRadius.Large)
                .background(AppColors.Surface)
                .padding(16.dp)
        ) {
            Text("Purchase ID", color = AppColors.TextHint, fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (receiptId.isNotBlank()) receiptId else "Not available",
                color = AppColors.TextDark,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(24.dp))

        PrimaryButton(
            text = "View Receipt",
            enabled = receiptId.isNotBlank(),
            onClick = { onDownloadReceipt(receiptId) }
        )

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onBackHome,
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = AppRadius.Medium,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Primary),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, AppColors.Primary)
        ) {
            Text("Back to Home", fontWeight = FontWeight.Bold)
        }
    }
}