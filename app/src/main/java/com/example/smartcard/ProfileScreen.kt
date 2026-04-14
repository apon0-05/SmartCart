package com.example.smartcard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onMyPurchases: () -> Unit,
    onBottomHome: () -> Unit,
    onBottomBag: () -> Unit,
    onBottomCart: () -> Unit,
    onBottomHistory: () -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser
    val userName =
        user?.displayName?.takeIf { it.isNotBlank() }
            ?: user?.email?.substringBefore("@")
            ?: "User"
    val userEmail = user?.email ?: "No email"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(AppColors.CardWarm),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = AppColors.Primary,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = userName,
                color = AppColors.TextDark,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = userEmail,
                color = AppColors.TextHint,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        ProfileMenuCard(
            icon = Icons.Default.PersonOutline,
            title = "Personal Data",
            subtitle = userEmail,
            onClick = { }
        )
        Spacer(modifier = Modifier.height(10.dp))
        ProfileMenuCard(
            icon = Icons.Default.NotificationsNone,
            title = "Notifications",
            badge = "2",
            onClick = { }
        )
        Spacer(modifier = Modifier.height(10.dp))
        ProfileMenuCard(
            icon = Icons.Default.ShoppingCartCheckout,
            title = "My purchases",
            onClick = onMyPurchases
        )
        Spacer(modifier = Modifier.height(10.dp))
        ProfileMenuCard(
            icon = Icons.Default.CreditCard,
            title = "My cards",
            onClick = { }
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(AppRadius.Large)
                .background(Color(0xFFFFF0EE))
                .clickable {
                    CartConnectionRepository.disconnectCurrentUserCart(
                        onSuccess = {
                            FirebaseAuth.getInstance().signOut()
                            onLogout()
                        },
                        onError = {
                            FirebaseAuth.getInstance().signOut()
                            onLogout()
                        }
                    )
                }
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = null,
                tint = AppColors.Error
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Sign Out",
                color = AppColors.Error,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        BottomNavBar(
            currentTab = NavTab.HOME,
            onHome = onBottomHome,
            onBag = onBottomBag,
            onCart = onBottomCart,
            onHistory = onBottomHistory
        )
    }
}

@Composable
private fun ProfileMenuCard(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    badge: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppRadius.Large)
            .background(AppColors.Surface)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(AppRadius.Medium)
                .background(AppColors.SurfaceAlt),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.TextDark,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    color = AppColors.TextDark,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (badge != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(AppColors.Primary)
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = badge,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(text = subtitle, color = AppColors.TextHint, fontSize = 13.sp)
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = AppColors.TextHint,
            modifier = Modifier.size(18.dp)
        )
    }
}