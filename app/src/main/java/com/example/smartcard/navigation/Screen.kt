package com.example.smartcard.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object SignUp : Screen("signup")
    data object Home : Screen("home")

    // Если захочешь — добавишь свои:
    data object Cart : Screen("cart")
    data object Camera : Screen("camera")
}