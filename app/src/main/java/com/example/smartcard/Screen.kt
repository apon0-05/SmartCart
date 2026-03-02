package com.example.smartcard

sealed class Screen(val route: String) {
    data object SignUp : Screen("signup")
    data object Login : Screen("login")

    object Home : Screen("home")

    object Camera : Screen("camera")

    data object Cart : Screen("cart")
}
