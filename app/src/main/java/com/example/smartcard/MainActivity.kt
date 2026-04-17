package com.example.smartcard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.produceState
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.smartcard.localization.LocalAppStrings
import com.example.smartcard.localization.provideStrings
import com.example.smartcard.navigation.AppNavGraph
import com.example.smartcard.navigation.Screen
import com.example.smartcard.utils.LanguageManager
import com.example.smartcard.utils.OnboardingPrefs
import com.google.firebase.auth.FirebaseAuth

private data class LaunchFlags(
    val languageSelected: Boolean,
    val onboardingCompleted: Boolean,
    val isLoggedIn: Boolean
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        LanguageManager.init(applicationContext)

        setContent {
            val currentLang by LanguageManager.language.collectAsState()
            val launchFlags by produceState<LaunchFlags?>(initialValue = null) {
                val languageSelected = OnboardingPrefs.isLanguageSelected(applicationContext)
                val onboardingCompleted = OnboardingPrefs.isOnboardingCompleted(applicationContext)
                val isLoggedIn = FirebaseAuth.getInstance().currentUser != null

                value = LaunchFlags(
                    languageSelected = languageSelected,
                    onboardingCompleted = onboardingCompleted,
                    isLoggedIn = isLoggedIn
                )
            }

            val strings = provideStrings(currentLang)
            CompositionLocalProvider(LocalAppStrings provides strings) {
                SmartCartTheme {
                    if (launchFlags == null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        val startDestination = remember(launchFlags) {
                            when {
                                !launchFlags!!.onboardingCompleted -> Screen.Onboarding.route
                                !launchFlags!!.languageSelected -> Screen.LanguageSelection.route
                                launchFlags!!.isLoggedIn -> Screen.Home.route
                                else -> Screen.Login.route
                            }
                        }
                        val navController = rememberNavController()
                        AppNavGraph(navController = navController, startDestination = startDestination)
                    }
                }
            }
        }
    }
}