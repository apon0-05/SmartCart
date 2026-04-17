package com.example.smartcard.navigation

sealed class Screen(val route: String) {
    data object LanguageSelection : Screen("language_selection")
    data object Onboarding : Screen("onboarding")
    data object Login : Screen("login")
    data object SignUp : Screen("signup")
    data object Home : Screen("home")

    // Если захочешь — добавишь свои:
    data object Cart : Screen("cart")
    data object Camera : Screen("camera")
    data object History : Screen("history")
    data object Profile : Screen("profile")
    data object PersonalData : Screen("personal_data")
    data object ScanCartQr : Screen("scan_cart_qr")
}