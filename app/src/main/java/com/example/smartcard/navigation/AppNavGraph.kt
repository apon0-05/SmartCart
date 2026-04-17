package com.example.smartcard.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.smartcard.CameraScreen
import com.example.smartcard.CartConnectedScreen
import com.example.smartcard.CartScreen
import com.example.smartcard.HomeScreen
import com.example.smartcard.LanguageSelectionScreen
import com.example.smartcard.LoginScreen
import com.example.smartcard.NavTab
import com.example.smartcard.NotificationsScreen
import com.example.smartcard.OnboardingScreen
import com.example.smartcard.PersonalDataScreen
import com.example.smartcard.ProfileScreen
import com.example.smartcard.PurchaseDetailScreen
import com.example.smartcard.PurchaseHistoryScreen
import com.example.smartcard.ReceiptScreen
import com.example.smartcard.ScanCartQrScreen
import com.example.smartcard.SignUpScreen
import com.example.smartcard.SuccessPaymentScreen
import com.example.smartcard.ui.components.SmartCartAnimatedBottomBar
import com.example.smartcard.utils.LanguageManager
import com.example.smartcard.utils.OnboardingPrefs
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val topLevelRoutes = setOf(
        Screen.Home.route,
        Screen.ScanCartQr.route,
        Screen.Cart.route,
        Screen.History.route
    )

    fun navigateToDetailIfNotCurrent(route: String) {
        if (navController.currentDestination?.route == route) return
        navController.navigate(route)
    }

    fun navigateToTopLevel(route: String) {
        if (currentRoute == route) return
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val bottomBarTab: NavTab? = when (currentRoute) {
        Screen.Home.route -> NavTab.HOME
        Screen.Cart.route -> NavTab.CART
        Screen.ScanCartQr.route -> NavTab.SCAN
        Screen.History.route -> NavTab.HISTORY
        else -> null
    }

    Scaffold(
        bottomBar = {
            if (bottomBarTab != null && currentRoute in topLevelRoutes) {
                SmartCartAnimatedBottomBar(
                    currentTab = bottomBarTab,
                    onHome = {
                        navigateToTopLevel(Screen.Home.route)
                    },
                    onScan = {
                        navigateToTopLevel(Screen.ScanCartQr.route)
                    },
                    onCart = {
                        navigateToTopLevel(Screen.Cart.route)
                    },
                    onHistory = {
                        navigateToTopLevel(Screen.History.route)
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.LanguageSelection.route) {
                LanguageSelectionScreen(
                    onLanguageSelected = { langCode ->
                        scope.launch {
                            LanguageManager.setLanguage(context, langCode)
                            OnboardingPrefs.setLanguageSelected(context, langCode)
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.LanguageSelection.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }

            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onSkip = {
                        scope.launch {
                            OnboardingPrefs.setOnboardingCompleted(context)
                            navController.navigate(Screen.LanguageSelection.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    },
                    onFinish = {
                        scope.launch {
                            OnboardingPrefs.setOnboardingCompleted(context)
                            navController.navigate(Screen.LanguageSelection.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onSignUp = {
                        if (navController.currentDestination?.route != Screen.SignUp.route) {
                            navController.navigate(Screen.SignUp.route) {
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }

            composable(Screen.SignUp.route) {
                SignUpScreen(
                    onSignUpSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onBackToLogin = {
                        val popped = navController.popBackStack()
                        if (!popped) {
                            navController.navigate(Screen.Login.route) {
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                val user = FirebaseAuth.getInstance().currentUser
                val safeName =
                    user?.displayName?.takeIf { it.isNotBlank() }
                        ?: user?.email?.substringBefore("@")
                        ?: "User"

                HomeScreen(
                    userFullName = safeName,

                    onProfileClick = { navigateToDetailIfNotCurrent(Screen.Profile.route) },

                    onScanCartClick = { navigateToTopLevel(Screen.ScanCartQr.route) },
                    onScanProductClick = { navigateToDetailIfNotCurrent(Screen.Camera.route) },
                    onReceiptClick = { navigateToTopLevel(Screen.History.route) },
                    onProductsPurchasedClick = { navigateToTopLevel(Screen.History.route) },
                    onNavigateToCleanState = {
                        navigateToTopLevel(Screen.Home.route)
                    }
                )
            }

            composable(Screen.Cart.route) {
                CartScreen(
                    onBack = { navController.popBackStack() },
                    onGoPayment = { receiptId ->
                        navController.navigate("success/$receiptId")
                    }
                )
            }
            composable(Screen.Camera.route) {
                CameraScreen(
                    onBack = { navController.popBackStack() },
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

            composable(Screen.History.route) {
                PurchaseHistoryScreen(
                    onBack = { navController.popBackStack() },
                    onOpenPurchase = { receiptId ->
                        navController.navigate("history_detail/$receiptId")
                    }
                )
            }

            composable("history_detail/{receiptId}") { backStackEntry ->
                val receiptId = backStackEntry.arguments?.getString("receiptId") ?: ""

                PurchaseDetailScreen(
                    receiptId = receiptId,
                    onBack = { navController.popBackStack() }
                )
            }


            composable(Screen.Profile.route) {
                ProfileScreen(
                    onBack = { navController.popBackStack() },
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onMyPurchases = {
                        navigateToTopLevel(Screen.History.route)
                    },
                    onNotifications = {
                        navigateToDetailIfNotCurrent("notifications")
                    },
                    onChangeLanguage = { lang ->
                        LanguageManager.setLanguage(context, lang)
                    },
                    onPersonalData = {
                        navigateToDetailIfNotCurrent(Screen.PersonalData.route)
                    },
                )
            }

            composable(Screen.PersonalData.route) {
                PersonalDataScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.ScanCartQr.route) {
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
                    cartIdArg = cartId,
                    onBackHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    }
                )
            }

            composable("notifications") {
                NotificationsScreen(
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}