package com.example.smartcard.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.smartcard.CameraScreen
import com.example.smartcard.CartConnectedScreen
import com.example.smartcard.CartScreen
import com.example.smartcard.HomeScreen
import com.example.smartcard.LoginScreen
import com.example.smartcard.ProfileScreen
import com.example.smartcard.PurchaseDetailScreen
import com.example.smartcard.PurchaseHistoryScreen
import com.example.smartcard.R
import com.example.smartcard.ReceiptScreen
import com.example.smartcard.ScanCartQrScreen
import com.example.smartcard.SignUpScreen
import com.example.smartcard.SuccessPaymentScreen
import com.example.smartcard.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String,
    onLanguageChange: (String) -> Unit
) {
    val authVm: AuthViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onSignUp = { navController.navigate(Screen.SignUp.route) }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable(Screen.Home.route) {
            val user = FirebaseAuth.getInstance().currentUser
            val safeName =
                user?.displayName?.takeIf { it.isNotBlank() }
                    ?: user?.email?.substringBefore("@")
                    ?: stringResource(R.string.user)

            HomeScreen(
                userFullName = safeName,

                onProfileClick = { navController.navigate("profile") },

                onScanProductClick = { navController.navigate(Screen.Camera.route) },
                onReceiptClick = { navController.navigate("history") }, // или свой экран
                onProductsPurchasedClick = { navController.navigate("history") }, // или свой экран

                onBottomHome = { /* уже Home */ },
                onBottomBag = { navController.navigate("scan_cart_qr") },
                onBottomCart = { navController.navigate(Screen.Cart.route) },
                onBottomHistory = {navController.navigate("history")}
            )
        }

        // Остальные, которые у тебя есть в проекте
        composable(Screen.Cart.route) {
            CartScreen(
                onBack = { navController.popBackStack() },
                onGoPayment = { receiptId ->
                    navController.navigate("success/$receiptId")
                              },
                onBottomHome = { navController.navigate(Screen.Home.route) },
                onBottomBag = { navController.navigate("scan_cart_qr") },
                onBottomCart = { /* already */ },
                onBottomHistory = { navController.navigate("history") }
            )
        }
        composable(Screen.Camera.route) {
            CameraScreen(
                onBack = { navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
                },
                onProductFoundGoCart = {
                    navController.navigate(Screen.Cart.route) {
                        popUpTo(Screen.Camera.route) { inclusive = true }
                    }
                }
            )
        }
        composable("success/{receiptId}") { backStackEntry ->
            val receiptId = backStackEntry.arguments?.getString("receiptId") ?: ""

            SuccessPaymentScreen(
                receiptId = receiptId,
                onDownloadReceipt = { id ->
                    navController.navigate("receipt/$id")
                },
                onBackHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                }
            )
        }

        composable("receipt/{receiptId}") { backStackEntry ->
            val receiptId = backStackEntry.arguments?.getString("receiptId") ?: ""

            ReceiptScreen(
                receiptId = receiptId,
                onBack = { navController.popBackStack() }
            )
        }

        composable("history") {
            PurchaseHistoryScreen(
                onBack = { navController.popBackStack() },
                onOpenPurchase = { receiptId ->
                    navController.navigate("history_detail/$receiptId")
                },
                onBottomHome = { navController.navigate(Screen.Home.route) },
                onBottomBag = { navController.navigate("scan_cart_qr") },
                onBottomCart = { navController.navigate(Screen.Cart.route) },
                onBottomHistory = { /* already here */ }
            )
        }

        composable("history_detail/{receiptId}") { backStackEntry ->
            val receiptId = backStackEntry.arguments?.getString("receiptId") ?: ""

            PurchaseDetailScreen(
                receiptId = receiptId,
                onBack = { navController.popBackStack() },
                onBottomHome = { navController.navigate(Screen.Home.route) },
                onBottomBag = { /* TODO */ },
                onBottomCart = { navController.navigate(Screen.Cart.route) },
                onBottomHistory = { navController.navigate("history") }
            )
        }


        composable("profile") {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onMyPurchases = {
                    navController.navigate("history")
                },
                onBottomHome = { navController.navigate(Screen.Home.route) },
                onBottomBag = {navController.navigate("scan_cart_qr") },
                onBottomCart = { navController.navigate(Screen.Cart.route) },
                onBottomHistory = { navController.navigate("history") }
            )
        }

        composable("scan_cart_qr") {
            ScanCartQrScreen(
                onBack = { navController.popBackStack() },
                onConnected = { cartId ->
                    navController.navigate("cart_connected/$cartId")
                }
            )
        }

        composable("cart_connected/{cartId}") { backStackEntry ->
            val cartId = backStackEntry.arguments?.getString("cartId") ?: ""

            CartConnectedScreen(
                onBackHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                }
            )
        }




    }
}