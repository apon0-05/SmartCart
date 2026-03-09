package com.example.smartcard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.navigation.compose.rememberNavController
import com.example.smartcard.navigation.AppNavGraph
import com.example.smartcard.navigation.Screen
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val start = if (FirebaseAuth.getInstance().currentUser != null) {
            Screen.Home.route
        } else {
            Screen.Login.route
        }

        setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                val navController = rememberNavController()
                AppNavGraph(navController = navController, startDestination = start)
            }
        }
    }
}