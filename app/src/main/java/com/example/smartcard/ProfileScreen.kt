package com.example.smartcard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material.icons.filled.CreditCard
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
    val bg = Color(0xFFF6F6F6)
    val textDark = Color(0xFF2F2F2F)
    val hint = Color(0xFF8A8A8A)
    val active = Color(0xFF29B6F6)

    val user = FirebaseAuth.getInstance().currentUser
    val userName =
        user?.displayName?.takeIf { it.isNotBlank() }
            ?: user?.email?.substringBefore("@")
            ?: "User"
    val userEmail = user?.email ?: "No email"

    var isLoggingOut by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Spacer(modifier = Modifier.height(26.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFB7B8FF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "👤",
                    fontSize = 38.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = userName,
                color = textDark,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = userEmail,
                color = hint,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        ProfileMenuCard(
            icon = {
                Icon(
                    imageVector = Icons.Default.PersonOutline,
                    contentDescription = null,
                    tint = textDark
                )
            },
            title = "Personal Data",
            subtitle = userEmail,
            onClick = { }
        )

        Spacer(modifier = Modifier.height(12.dp))

        ProfileMenuCard(
            icon = {
                Icon(
                    imageVector = Icons.Default.NotificationsNone,
                    contentDescription = null,
                    tint = textDark
                )
            },
            title = "Notifications",
            badge = "2",
            onClick = { }
        )

        Spacer(modifier = Modifier.height(12.dp))

        ProfileMenuCard(
            icon = {
                Icon(
                    imageVector = Icons.Default.ShoppingCartCheckout,
                    contentDescription = null,
                    tint = textDark
                )
            },
            title = "My purchases",
            onClick = onMyPurchases
        )

        Spacer(modifier = Modifier.height(12.dp))

        ProfileMenuCard(
            icon = {
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = null,
                    tint = textDark
                )
            },
            title = "My cards",
            onClick = { }
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
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
                .padding(horizontal = 18.dp, vertical = 22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = null,
                tint = textDark
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Exit",
                color = textDark,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        BottomNavBar(
            onHome = onBottomHome,
            onBag = onBottomBag,
            onCart = onBottomCart,
            onHistory = onBottomHistory
        )
    }
}

@Composable
private fun ProfileMenuCard(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String? = null,
    badge: String? = null,
    onClick: () -> Unit
) {
    val textDark = Color(0xFF2F2F2F)
    val hint = Color(0xFF8A8A8A)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(28.dp),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    color = textDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                if (badge != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFFF6A00))
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = badge,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = hint,
                    fontSize = 14.sp
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = textDark
        )
    }
}