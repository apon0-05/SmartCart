package com.example.smartcard

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.example.smartcard.localization.LocalAppStrings
import com.example.smartcard.localization.provideStrings
import com.example.smartcard.navigation.AppNavGraph
import com.example.smartcard.navigation.Screen
import com.example.smartcard.utils.LanguageManager
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        val start = if (FirebaseAuth.getInstance().currentUser != null) {
            Screen.Home.route
        } else {
            Screen.Login.route
        }

        setContent {

            var currentLang by remember {
                mutableStateOf(LanguageManager.getLanguage())
            }

            val strings = provideStrings(currentLang)

            CompositionLocalProvider(
                LocalAppStrings provides strings
            ) {
                MaterialTheme {
                    val navController = rememberNavController()

                    AppNavGraph(
                        navController = navController,
                        startDestination = start,
                        onLanguageChange = { lang ->
                            currentLang = lang
                            LanguageManager.setLanguage(lang)
                        }
                    )
                }
            }
        }
    }
}