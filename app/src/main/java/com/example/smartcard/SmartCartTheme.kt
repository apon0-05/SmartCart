package com.example.smartcard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Design Tokens ────────────────────────────────────────────────────────────

object AppColors {
    val Primary     = Color(0xFFCF6B2D)
    val PrimaryLight= Color(0xFFF1C2A6)
    val Background  = Color(0xFFF6F6F6)
    val Surface     = Color.White
    val SurfaceAlt  = Color(0xFFF4F4F4)
    val CardWarm    = Color(0xFFF3E9E6)
    val TextDark    = Color(0xFF2F2F2F)
    val TextHint    = Color(0xFF8A8A8A)
    val Success     = Color(0xFF2E9E5B)
    val SuccessBg   = Color(0xFFEAF7F0)
    val Error       = Color(0xFFD32F2F)
    // Keep Home screen bottom bar look as baseline across app
    val NavActive   = Color(0xFF2DA1FF)
    val NavActiveBg = Color(0xFFE1F0FF)
    val NavInactive = Color(0xFF9A9A9A)

    // Extended UI tokens for screen-level consistency
    val TextSubtle  = Color(0xFF7A7A7A)
    val SoftBlue    = Color(0xFFE8F0FF)
    val SoftOrange  = Color(0xFFFFE9DF)
    val SoftMint    = Color(0xFFE6F7F1)
    val AvatarBlue  = Color(0xFFDDE3FF)
    val DotInactive = Color(0xFFE4B9A8)
    val ProfileAvatar = Color(0xFFB7B8FF)
    val BadgeOrange = Color(0xFFFF6A00)

    // Auth / utility tokens
    val LoginSurface = Color(0xFFFAFAFA)
    val LoginHeaderBottom = Color(0xFFF5D7C7)
    val DividerMuted = Color(0xFFD7D7D7)
    val LabelOrange = Color(0xFFE07A46)
    val FieldBorder = Color(0xFF7A6D76)
    val DisabledStart = Color(0xFFE6E6E6)
    val DisabledEnd = Color(0xFFD9D9D9)
    val DisabledText = Color(0xFF9B9B9B)
    val GoogleBorder = Color(0xFFE3E3E3)
    val GoogleChipBg = Color(0xFFF1F1F1)
    val OutlineSoft = Color(0xFFB0B0B0)
    val ErrorBg = Color(0xFFFFF3F3)
    val Transparent = Color.Transparent
    val OverlayWhite33 = Color(0x33FFFFFF)
    val NotificationBorder = Color(0xFFE2E2E2)
    val LinkBlue = Color(0xFF4F7CFF)

    // Sign-up palette tokens
    val SignUpBg = Color(0xFFF8F8F8)
    val PeachLight = Color(0xFFF7D8C7)
    val PeachDark = Color(0xFFE97B39)
    val CheckboxUnchecked = Color(0xFFCFCFCF)

    // Disabled/control state tokens
    val PrimaryDisabledStart = Color(0xFFCCCCCC)
    val PrimaryDisabledEnd = Color(0xFFAAAAAA)

    // Dark camera/scan surfaces
    val DarkSurface = Color(0xFF000000)
    val OnDark = Color.White
}

object AppRadius {
    val Small  = RoundedCornerShape(12.dp)
    val Medium = RoundedCornerShape(16.dp)
    val Large  = RoundedCornerShape(24.dp)
    val Circle = RoundedCornerShape(50)
}

val PrimaryGradient get() = Brush.horizontalGradient(
    listOf(AppColors.PrimaryLight, AppColors.Primary)
)

// ─── Shared Composables ───────────────────────────────────────────────────────

@Composable
fun AppBackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(42.dp)
            .clip(AppRadius.Medium)
            .background(AppColors.Surface)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.ArrowBackIosNew,
            contentDescription = "Back",
            tint = AppColors.TextDark,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(AppRadius.Medium)
            .background(
                if (enabled) PrimaryGradient
                else Brush.horizontalGradient(listOf(AppColors.PrimaryDisabledStart, AppColors.PrimaryDisabledEnd))
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = AppColors.Surface,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun AppLoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = AppColors.Primary, strokeWidth = 3.dp)
            Spacer(Modifier.height(12.dp))
            Text("Loading...", color = AppColors.TextHint, fontSize = 14.sp)
        }
    }
}

@Composable
fun AppErrorState(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppRadius.Medium)
            .background(AppColors.ErrorBg)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.WarningAmber,
            contentDescription = null,
            tint = AppColors.Error,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = message,
            color = AppColors.Error,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        if (onRetry != null) {
            TextButton(onClick = onRetry) {
                Text("Try again", color = AppColors.Primary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ─── BottomNavBar (route-aware) ───────────────────────────────────────────────

enum class NavTab { HOME, SCAN, CART, HISTORY }

@Composable
fun BottomNavBar(
    currentTab: NavTab = NavTab.HOME,
    onHome: () -> Unit,
    onBag: () -> Unit,
    onCart: () -> Unit,
    onHistory: () -> Unit
) {
    Row(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
            .fillMaxWidth()
            .height(64.dp)
            .clip(AppRadius.Large)
            .background(AppColors.SurfaceAlt)
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavTabItem(
            icon = Icons.Outlined.Home,
            label = "Home",
            active = currentTab == NavTab.HOME,
            onClick = onHome
        )
        NavTabItem(
            icon = Icons.Outlined.ShoppingBag,
            label = "Scan",
            active = currentTab == NavTab.SCAN,
            onClick = onBag
        )
        NavTabItem(
            icon = Icons.Outlined.ShoppingCart,
            label = "Cart",
            active = currentTab == NavTab.CART,
            onClick = onCart
        )
        NavTabItem(
            icon = Icons.Outlined.History,
            label = "History",
            active = currentTab == NavTab.HISTORY,
            onClick = onHistory
        )
    }
}

@Composable
private fun NavTabItem(
    icon: ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    val bg  = if (active) AppColors.NavActiveBg else AppColors.Transparent
    val tint = if (active) AppColors.NavActive else AppColors.NavInactive

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(AppRadius.Medium)
            .background(bg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
    }
}

// ─── Theme ────────────────────────────────────────────────────────────────────

private val LightColors = androidx.compose.material3.lightColorScheme(
    primary = AppColors.Primary,
    onPrimary = AppColors.Surface,
    secondary = AppColors.Primary,
    onSecondary = AppColors.Surface,
    background = AppColors.Background,
    onBackground = AppColors.TextDark,
    surface = AppColors.Surface,
    onSurface = AppColors.TextDark,
    surfaceVariant = AppColors.SurfaceAlt,
    outline = AppColors.OutlineSoft
)

private val AppTypography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, lineHeight = 34.sp),
    displayMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp)
)

private val AppShapes = androidx.compose.material3.Shapes(
    small  = AppRadius.Small,
    medium = AppRadius.Medium,
    large  = AppRadius.Large
)

@Composable
fun SmartCartTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
