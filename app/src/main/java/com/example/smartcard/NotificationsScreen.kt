package com.example.smartcard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartcard.localization.LocalAppStrings

data class NotificationItem(
    val title: String,
    val subtitle: String,
    val type: String
)

@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
) {
    val texts = LocalAppStrings.current
    val titleColor = AppColors.TextDark
    val subtitleColor = AppColors.TextSubtle
    val orange = AppColors.BadgeOrange
    val borderColor = AppColors.NotificationBorder

    val notifications = listOf(
        NotificationItem(
            title = texts.notifPromoTitle,
            subtitle = texts.notifPromoSubtitle,
            type = "promo"
        ),
        NotificationItem(
            title = texts.notifNewsTitle,
            subtitle = texts.notifNewsSubtitle,
            type = "news"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(AppColors.Surface)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "‹",
                        fontSize = 22.sp,
                        color = AppColors.TextDark
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = texts.notificationsTitle,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AppColors.TextDark
                )
            }

            Spacer(modifier = Modifier.height(34.dp))

            notifications.forEachIndexed { index, item ->
                NotificationCard(
                    item = item,
                    orange = orange,
                    titleColor = titleColor,
                    subtitleColor = subtitleColor,
                    borderColor = borderColor
                )

                if (index != notifications.lastIndex) {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun NotificationCard(
    item: NotificationItem,
    orange: Color,
    titleColor: Color,
    subtitleColor: Color,
    borderColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AppColors.Surface)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(orange),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (item.type == "promo") {
                    Icons.Outlined.LocalOffer
                } else {
                    Icons.Outlined.Campaign
                },
                contentDescription = null,
                tint = AppColors.Surface,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = item.title,
                color = titleColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = item.subtitle,
                color = subtitleColor,
                fontSize = 14.sp
            )
        }
    }
}