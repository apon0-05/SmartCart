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

data class NotificationItem(
    val title: String,
    val subtitle: String,
    val type: String
)

@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onBottomHome: () -> Unit,
    onBottomBag: () -> Unit,
    onBottomCart: () -> Unit,
    onBottomHistory: () -> Unit
) {
    val bg = Color(0xFFF6F6F6)
    val containerBg = Color.White
    val titleColor = Color(0xFF2F2F2F)
    val subtitleColor = Color(0xFF7A7A7A)
    val orange = Color(0xFFFF7A00)
    val borderColor = Color(0xFFE2E2E2)

    val notifications = listOf(
        NotificationItem(
            title = "Promocodes",
            subtitle = "2000tg discount for first order",
            type = "promo"
        ),
        NotificationItem(
            title = "News",
            subtitle = "0.5 fuse tea = 1tg",
            type = "news"
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(26.dp))
                .background(containerBg)
                .padding(horizontal = 18.dp, vertical = 18.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // 🔙 КНОПКА НАЗАД
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "‹",
                        fontSize = 22.sp,
                        color = Color(0xFF2F2F2F)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Notifications",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2F2F2F)
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
            .background(Color.White)
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
                tint = Color.White,
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