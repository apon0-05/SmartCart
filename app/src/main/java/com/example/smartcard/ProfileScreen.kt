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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import com.example.smartcard.utils.LanguageManager
import com.google.firebase.auth.FirebaseAuth

data class ProfileTexts(
    val user: String,
    val noEmail: String,
    val personalData: String,
    val notifications: String,
    val myPurchases: String,
    val myCards: String,
    val exit: String,
    val language: String,
    val chooseAppLanguage: String,
    val english: String,
    val russian: String,
    val kazakh: String
)

private fun profileTexts(lang: String): ProfileTexts {
    return when (lang) {
        "ru" -> ProfileTexts(
            user = "Пользователь",
            noEmail = "Нет email",
            personalData = "Личные данные",
            notifications = "Уведомления",
            myPurchases = "Мои покупки",
            myCards = "Мои карты",
            exit = "Выйти",
            language = "Язык",
            chooseAppLanguage = "Выберите язык приложения",
            english = "English",
            russian = "Русский",
            kazakh = "Қазақша"
        )
        "kk" -> ProfileTexts(
            user = "Пайдаланушы",
            noEmail = "Email жоқ",
            personalData = "Жеке деректер",
            notifications = "Хабарламалар",
            myPurchases = "Сатып алуларым",
            myCards = "Карталарым",
            exit = "Шығу",
            language = "Тіл",
            chooseAppLanguage = "Қосымша тілін таңдаңыз",
            english = "English",
            russian = "Русский",
            kazakh = "Қазақша"
        )
        else -> ProfileTexts(
            user = "User",
            noEmail = "No email",
            personalData = "Personal Data",
            notifications = "Notifications",
            myPurchases = "My purchases",
            myCards = "My cards",
            exit = "Exit",
            language = "Language",
            chooseAppLanguage = "Choose app language",
            english = "English",
            russian = "Русский",
            kazakh = "Қазақша"
        )
    }
}

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

    var currentLang by remember { mutableStateOf(LanguageManager.getLanguage()) }
    val texts = profileTexts(currentLang)

    val user = FirebaseAuth.getInstance().currentUser
    val userName =
        user?.displayName?.takeIf { it.isNotBlank() }
            ?: user?.email?.substringBefore("@")
            ?: texts.user

    val userEmail = user?.email ?: texts.noEmail

    var isLoggingOut by remember { mutableStateOf(false) }

    key(currentLang) {
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
                title = texts.personalData,
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
                title = texts.notifications,
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
                title = texts.myPurchases,
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
                title = texts.myCards,
                onClick = { }
            )

            Spacer(modifier = Modifier.height(16.dp))

            LanguageSwitcherCard(
                currentLang = currentLang,
                texts = texts,
                onLanguageSelected = { lang ->
                    currentLang = lang
                    LanguageManager.setLanguage(lang)
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .clickable {
                        if (!isLoggingOut) {
                            isLoggingOut = true
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
                    text = texts.exit,
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
}

@Composable
private fun LanguageSwitcherCard(
    currentLang: String,
    texts: ProfileTexts,
    onLanguageSelected: (String) -> Unit
) {
    val textDark = Color(0xFF2F2F2F)
    val hint = Color(0xFF8A8A8A)
    val selectedBg = Color(0xFFE8F1FF)
    val selectedBorder = Color(0xFF4A90E2)
    val normalBorder = Color(0xFFE0E0E0)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(18.dp)
    ) {
        Text(
            text = texts.language,
            color = textDark,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = texts.chooseAppLanguage,
            color = hint,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LanguageChip(
                modifier = Modifier.weight(1f),
                text = "EN",
                label = texts.english,
                selected = currentLang == "en",
                selectedBg = selectedBg,
                selectedBorder = selectedBorder,
                normalBorder = normalBorder,
                onClick = { onLanguageSelected("en") }
            )

            LanguageChip(
                modifier = Modifier.weight(1f),
                text = "RU",
                label = texts.russian,
                selected = currentLang == "ru",
                selectedBg = selectedBg,
                selectedBorder = selectedBorder,
                normalBorder = normalBorder,
                onClick = { onLanguageSelected("ru") }
            )

            LanguageChip(
                modifier = Modifier.weight(1f),
                text = "KZ",
                label = texts.kazakh,
                selected = currentLang == "kk",
                selectedBg = selectedBg,
                selectedBorder = selectedBorder,
                normalBorder = normalBorder,
                onClick = { onLanguageSelected("kk") }
            )
        }
    }
}

@Composable
private fun LanguageChip(
    modifier: Modifier = Modifier,
    text: String,
    label: String,
    selected: Boolean,
    selectedBg: Color,
    selectedBorder: Color,
    normalBorder: Color,
    onClick: () -> Unit
) {
    val textDark = Color(0xFF2F2F2F)
    val hint = Color(0xFF8A8A8A)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) selectedBg else Color.White)
            .border(
                width = 1.5.dp,
                color = if (selected) selectedBorder else normalBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            color = textDark,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            color = hint,
            fontSize = 12.sp
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