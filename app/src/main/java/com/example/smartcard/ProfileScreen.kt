package com.example.smartcard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartcard.localization.LocalAppStrings
import com.example.smartcard.utils.LanguageManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private fun resolveAvatarRes(gender: String?): Int {
    return if (gender?.trim()?.equals("male", ignoreCase = true) == true) {
        R.drawable.avatar_male
    } else {
        R.drawable.avatar_female
    }
}

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onMyPurchases: () -> Unit,
    onNotifications: () -> Unit,
    onChangeLanguage: (String) -> Unit,
    onPersonalData: () -> Unit,
) {
    val texts = LocalAppStrings.current
    val bg = AppColors.Background
    val textDark = AppColors.TextDark

    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()
    val userName =
        user?.displayName?.takeIf { it.isNotBlank() }
            ?: user?.email?.substringBefore("@")
            ?: texts.user
    val userEmail = user?.email ?: texts.noEmail
    val selectedLang by LanguageManager.language.collectAsState()
    var isLoggingOut by remember { mutableStateOf(false) }
    var showLangDialog by remember { mutableStateOf(false) }
    var avatarGender by remember { mutableStateOf("female") }

    DisposableEffect(user?.uid) {
        val uid = user?.uid ?: return@DisposableEffect onDispose { }
        val registration = db.collection("users")
            .document(uid)
            .addSnapshotListener { document, _ ->
                val rawGender = document?.getString("avatarGender")?.trim()?.lowercase()
                val normalizedGender = when (rawGender) {
                    "male" -> "male"
                    "female" -> "female"
                    else -> "female"
                }

                avatarGender = normalizedGender
            }

        onDispose {
            registration.remove()
        }
    }

    val langDisplayName = when (selectedLang) {
        "ru" -> "Русский"
        "kk" -> "Қазақша"
        else -> "English"
    }

    if (showLangDialog) {
        AlertDialog(
            onDismissRequest = { showLangDialog = false },
            confirmButton = {
                Text(
                    text = texts.cancel,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { showLangDialog = false }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    color = AppColors.TextHint,
                    fontWeight = FontWeight.Medium
                )
            },
            title = {
                Text(
                    text = texts.selectLanguage,
                    fontWeight = FontWeight.Bold,
                    color = textDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "kk" to "Қазақша",
                        "ru" to "Русский",
                        "en" to "English",
                    ).forEach { (code, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selectedLang == code) AppColors.SoftOrange else Color.Transparent)
                                .clickable {
                                    if (selectedLang != code) onChangeLanguage(code)
                                    showLangDialog = false
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(if (selectedLang == code) AppColors.Primary else AppColors.SurfaceAlt),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedLang == code) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(text = label, color = textDark, fontSize = 16.sp)
                        }
                    }
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 0.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppBackButton(onClick = onBack)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(AppColors.ProfileAvatar.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = resolveAvatarRes(avatarGender)),
                        contentDescription = null,
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Fit
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = userName,
                    color = textDark,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = true
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = userEmail,
                    color = AppColors.TextHint,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = true
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            ProfileMenuCard(
                icon = {
                    Icon(imageVector = Icons.Outlined.PersonOutline, contentDescription = null, tint = textDark)
                },
                title = texts.personalData,
                subtitle = userEmail,
                onClick = onPersonalData
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileMenuCard(
                icon = {
                    Icon(imageVector = Icons.Outlined.NotificationsNone, contentDescription = null, tint = textDark)
                },
                title = texts.notifications,
                badge = "2",
                onClick = onNotifications
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileMenuCard(
                icon = {
                    Icon(imageVector = Icons.Outlined.Inventory2, contentDescription = null, tint = textDark)
                },
                title = texts.myPurchases,
                onClick = onMyPurchases
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileMenuCard(
                icon = {
                    Icon(imageVector = Icons.Outlined.CreditCard, contentDescription = null, tint = textDark)
                },
                title = texts.myCards,
                onClick = { }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileMenuCard(
                icon = {
                    Icon(imageVector = Icons.Outlined.Language, contentDescription = null, tint = textDark)
                },
                title = texts.language,
                subtitle = langDisplayName,
                onClick = { showLangDialog = true }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Logout row ───────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppColors.Surface)
                    .clickable(enabled = !isLoggingOut) {
                        isLoggingOut = true
                        val uid = FirebaseAuth.getInstance().currentUser?.uid
                        if (!uid.isNullOrBlank()) {
                            SmartCartUiCache.clearUser(uid)
                        }
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
                    .padding(horizontal = 18.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Logout,
                    contentDescription = null,
                    tint = AppColors.Error
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = texts.exit,
                    color = AppColors.Error,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
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
    val textDark = AppColors.TextDark
    val hint = AppColors.TextHint

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(AppColors.Surface)
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
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = true
                )

                if (badge != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(AppColors.BadgeOrange)
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = badge,
                            color = AppColors.Surface,
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
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = true
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