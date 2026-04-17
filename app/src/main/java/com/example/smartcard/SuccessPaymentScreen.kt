package com.example.smartcard

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            AppBackButton(onClick = onBackHome)
        }

        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                AppColors.Primary.copy(alpha = 0.20f),
                                AppColors.Primary.copy(alpha = 0.06f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(122.dp)
                        .clip(CircleShape)
                        .background(AppColors.Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_success_check),
                        contentDescription = null,
                        modifier = Modifier.size(62.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = texts.successful,
                color = AppColors.TextDark,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 30.sp,
                lineHeight = 34.sp,
                letterSpacing = 0.15.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = texts.paymentSuccessDesc,
                color = AppColors.TextHint,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                letterSpacing = 0.1.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(AppColors.PrimaryLight, AppColors.Primary)
                    )
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
                fontSize = 16.sp,
                letterSpacing = 0.1.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (receiptId.isBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = texts.receiptUnavailable,
                color = AppColors.Error,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}
